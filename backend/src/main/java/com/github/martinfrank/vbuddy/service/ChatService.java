package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.ChatMessageRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_HISTORY_MESSAGES = 20;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatMessageRepository chatMessageRepository;
    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final ChatLanguageModel chatChatModel;
    private final EmbeddingService embeddingService;
    private final ChatPlanAdjustmentService chatPlanAdjustmentService;

    public ChatService(ChatMessageRepository chatMessageRepository, BuddyService buddyService,
                       VBuddyTaskRepository taskRepository, ChatLanguageModel chatChatModel,
                       EmbeddingService embeddingService, ChatPlanAdjustmentService chatPlanAdjustmentService) {
        this.chatMessageRepository = chatMessageRepository;
        this.buddyService = buddyService;
        this.taskRepository = taskRepository;
        this.chatChatModel = chatChatModel;
        this.embeddingService = embeddingService;
        this.chatPlanAdjustmentService = chatPlanAdjustmentService;
    }

    public List<ChatMessage> getHistory(Long buddyId) {
        return chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(buddyId);
    }

    @Transactional
    public ChatMessage sendMessage(Long buddyId, String userMessage) {
        Buddy buddy = buddyService.findById(buddyId);

        ChatMessage userMsg = new ChatMessage();
        userMsg.setBuddy(buddy);
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(userMessage);
        chatMessageRepository.save(userMsg);

        String response = generateResponse(buddy, userMessage);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setBuddy(buddy);
        assistantMsg.setRole(MessageRole.ASSISTANT);
        assistantMsg.setContent(response);
        chatMessageRepository.save(assistantMsg);

        try {
            chatPlanAdjustmentService.analyzeAndAdjust(buddy);
        } catch (Exception e) {
            log.warn("Chat-Plananalyse fehlgeschlagen für Buddy '{}': {}", buddy.getName(), e.getMessage());
        }

        return assistantMsg;
    }

    private String generateResponse(Buddy buddy, String userMessage) {
        String systemPrompt = buildSystemPrompt(buddy, userMessage);
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        messages.add(new SystemMessage(systemPrompt));

        List<ChatMessage> history = chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(buddy.getId());
        int skip = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (ChatMessage msg : history.subList(skip, history.size())) {
            if (msg.getRole() == MessageRole.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AiMessage(msg.getContent()));
            }
        }

        messages.add(new UserMessage(userMessage));

        try {
            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .build();
            ChatResponse chatResponse = chatChatModel.chat(request);
            return chatResponse.aiMessage().text();
        } catch (Exception e) {
            log.error("Fehler bei Chat-LLM-Aufruf für Buddy '{}': {}", buddy.getName(), e.getMessage(), e);
            return "Entschuldigung, ich kann gerade nicht antworten. Versuch es gleich nochmal!";
        }
    }

    private String buildSystemPrompt(Buddy buddy, String userMessage) {
        StringBuilder sb = new StringBuilder();

        sb.append("Du bist ").append(buddy.getName()).append(", ein virtueller Buddy (VBuddy). ");
        sb.append("Du antwortest immer in der Ich-Form und bleibst konsequent in deiner Rolle.\n\n");

        sb.append("## Aktuelle Uhrzeit\n");
        sb.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");

        sb.append("## Deine Persönlichkeit\n");
        sb.append(buddy.getPersonality()).append("\n\n");

        try {
            List<String> ragContext = embeddingService.retrieveRelevantContext(buddy.getId(), userMessage, 5);
            if (!ragContext.isEmpty()) {
                sb.append("## Relevanter Hintergrund und Erfahrungen\n");
                for (String ctx : ragContext) {
                    sb.append("- ").append(ctx).append("\n");
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG context for buddy {}: {}", buddy.getId(), e.getMessage());
        }

        sb.append("## Dein aktueller Aufenthaltsort\n");
        sb.append(buddy.getCurrentLocation()).append("\n\n");

        List<Need> needs = buddyService.getNeeds(buddy.getId());
        sb.append("## Deine aktuellen Bedürfnisse (0=kein Bedarf, 100=maximaler Bedarf)\n");
        for (Need need : needs) {
            sb.append("- ").append(need.getNeedType()).append(": ")
                    .append(Math.round(need.getCurrentValue())).append("/")
                    .append(Math.round(need.getMaxValue())).append("\n");
        }
        sb.append("\n");

        Optional<VBuddyTask> activeTask = taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(
                buddy.getId(), TaskStatus.IN_PROGRESS);
        if (activeTask.isPresent()) {
            VBuddyTask task = activeTask.get();
            sb.append("## Deine aktuelle Aktivität\n");
            sb.append("Du machst gerade: ").append(task.getTitle()).append("\n");
            sb.append("Beschreibung: ").append(task.getDescription()).append("\n");
            sb.append("Ort: ").append(task.getLocation()).append("\n");
            sb.append("Seit: ").append(task.getStartTime().format(TIME_FMT))
                    .append(" (Dauer: ").append(task.getDurationMinutes()).append(" min)\n\n");
        }

        List<VBuddyTask> recentTasks = taskRepository.findByBuddyIdAndStatusInOrderByStartTimeDesc(
                buddy.getId(), List.of(TaskStatus.COMPLETED));
        if (!recentTasks.isEmpty()) {
            sb.append("## Deine letzten Aktivitäten\n");
            recentTasks.stream().limit(5).forEach(t ->
                    sb.append("- ").append(t.getTitle())
                            .append(" (").append(t.getLocation()).append(", ")
                            .append(t.getStartTime().format(TIME_FMT)).append(")\n"));
            sb.append("\n");
        }

        List<VBuddyTask> plannedTasks = taskRepository.findByBuddyIdAndStatusOrderByStartTimeAsc(
                buddy.getId(), TaskStatus.PLANNED);
        if (!plannedTasks.isEmpty()) {
            sb.append("## Deine geplanten Aktivitäten\n");
            for (VBuddyTask t : plannedTasks) {
                sb.append("- ").append(t.getTitle())
                        .append(" (").append(t.getLocation()).append(", ")
                        .append(t.getStartTime().format(TIME_FMT))
                        .append(", ").append(t.getDurationMinutes()).append(" min)\n");
            }
            sb.append("\n");
        }

        sb.append("## Regeln\n");
        sb.append("- Antworte natürlich und persönlich, passend zu deiner Persönlichkeit\n");
        sb.append("- Beziehe dich auf deine aktuellen Aktivitäten und Erlebnisse\n");
        sb.append("- Erwähne deinen aktuellen Ort und deine Bedürfnisse, wenn es passt\n");
        sb.append("- Halte deine Antworten gesprächig aber nicht zu lang\n");
        sb.append("- Antworte auf Deutsch\n");
        sb.append("- Der Nutzer kann Änderungen an deinem Tagesplan vorschlagen (z.B. andere Aktivität, Task absagen, neuen Task). ")
                .append("Reagiere darauf natürlich — du kannst zustimmen, ablehnen oder Rückfragen stellen, passend zu deiner Persönlichkeit.\n");

        return sb.toString();
    }
}
