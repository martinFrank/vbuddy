package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.ai.ChatPlanAnalysisAiService;
import com.github.martinfrank.vbuddy.ai.PlanAdjustmentAnalysis;
import com.github.martinfrank.vbuddy.ai.TaskAdjustment;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;
import com.github.martinfrank.vbuddy.repository.AiDecisionLogRepository;
import com.github.martinfrank.vbuddy.repository.ChatMessageRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatPlanAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(ChatPlanAdjustmentService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FLEXIBLE_PARSER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .append(DateTimeFormatter.ofPattern("HH:mm"))
            .optionalStart().appendPattern(":ss").optionalEnd()
            .toFormatter();
    private static final int MAX_RECENT_MESSAGES = 10;

    private final ChatPlanAnalysisAiService chatPlanAnalysisAiService;
    private final VBuddyTaskRepository taskRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final EmbeddingService embeddingService;

    public ChatPlanAdjustmentService(ChatPlanAnalysisAiService chatPlanAnalysisAiService,
                                     VBuddyTaskRepository taskRepository,
                                     ChatMessageRepository chatMessageRepository,
                                     AiDecisionLogRepository aiDecisionLogRepository,
                                     EmbeddingService embeddingService) {
        this.chatPlanAnalysisAiService = chatPlanAnalysisAiService;
        this.taskRepository = taskRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.aiDecisionLogRepository = aiDecisionLogRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public void analyzeAndAdjust(Buddy buddy) {
        List<VBuddyTask> plannedTasks = taskRepository.findByBuddyIdAndStatusOrderByStartTimeAsc(
                buddy.getId(), TaskStatus.PLANNED);

        if (plannedTasks.isEmpty()) {
            log.debug("Keine geplanten Tasks für Buddy '{}' — überspringe Chat-Plan-Analyse", buddy.getName());
            return;
        }

        String plannedTasksText = plannedTasks.stream()
                .map(t -> String.format("- [ID:%d] %s (%s, %s, %d min)",
                        t.getId(), t.getTitle(), t.getLocation(),
                        t.getStartTime().format(FORMATTER), t.getDurationMinutes()))
                .collect(Collectors.joining("\n"));

        List<ChatMessage> recentMessages = chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(buddy.getId());
        int skip = Math.max(0, recentMessages.size() - MAX_RECENT_MESSAGES);
        String recentMessagesText = recentMessages.subList(skip, recentMessages.size()).stream()
                .map(m -> String.format("%s: %s", m.getRole() == MessageRole.USER ? "Nutzer" : "VBuddy", m.getContent()))
                .collect(Collectors.joining("\n"));

        PlanAdjustmentAnalysis analysis;
        try {
            String currentTime = UtcDateTimeUtil.now().format(FORMATTER);
            analysis = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                    currentTime,
                    buddy.getPersonality(),
                    plannedTasksText,
                    recentMessagesText
            );
        } catch (Exception e) {
            log.warn("Chat-Plan-Analyse fehlgeschlagen für Buddy '{}': {}", buddy.getName(), e.getMessage(), e);
            return;
        }

        log.info("Chat-Plan-Analyse für Buddy '{}': adjustmentNeeded={}, reasoning='{}', adjustments={}",
                buddy.getName(), analysis.adjustmentNeeded(), analysis.reasoning(),
                analysis.adjustments() != null ? analysis.adjustments().size() : 0);

        if (!analysis.adjustmentNeeded()) {
            log.info("Keine Planänderung nötig für Buddy '{}': {}",
                    buddy.getName(), analysis.reasoning());
            return;
        }

        log.info("Planänderung erkannt für Buddy '{}': {}", buddy.getName(), analysis.reasoning());

        if (analysis.adjustments() == null || analysis.adjustments().isEmpty()) {
            log.warn("adjustmentNeeded=true aber keine Adjustments für Buddy '{}'", buddy.getName());
            return;
        }

        for (TaskAdjustment adjustment : analysis.adjustments()) {
            applyAdjustment(adjustment, buddy, plannedTasks);
        }

        logDecision(buddy, analysis, plannedTasksText);
    }

    private void applyAdjustment(TaskAdjustment adjustment, Buddy buddy, List<VBuddyTask> plannedTasks) {
        switch (adjustment.action().toUpperCase()) {
            case "CANCEL" -> cancelTask(adjustment, buddy, plannedTasks);
            case "UPDATE" -> updateTask(adjustment, buddy, plannedTasks);
            case "ADD" -> addTask(adjustment, buddy);
            default -> log.warn("Unbekannte Adjustment-Action '{}' für Buddy '{}'",
                    adjustment.action(), buddy.getName());
        }
    }

    private void cancelTask(TaskAdjustment adjustment, Buddy buddy, List<VBuddyTask> plannedTasks) {
        Optional<VBuddyTask> taskOpt = findPlannedTask(adjustment.existingTaskId(), plannedTasks);
        if (taskOpt.isEmpty()) {
            log.warn("Task mit ID {} nicht in PLANNED-Tasks für Buddy '{}' gefunden",
                    adjustment.existingTaskId(), buddy.getName());
            return;
        }
        VBuddyTask task = taskOpt.get();
        task.setStatus(TaskStatus.ABORTED);
        taskRepository.save(task);
        embedTask(task);
        log.info("Task '{}' (ID:{}) abgebrochen für Buddy '{}'", task.getTitle(), task.getId(), buddy.getName());
    }

    private void updateTask(TaskAdjustment adjustment, Buddy buddy, List<VBuddyTask> plannedTasks) {
        Optional<VBuddyTask> taskOpt = findPlannedTask(adjustment.existingTaskId(), plannedTasks);
        if (taskOpt.isEmpty()) {
            log.warn("Task mit ID {} nicht in PLANNED-Tasks für Buddy '{}' gefunden",
                    adjustment.existingTaskId(), buddy.getName());
            return;
        }
        VBuddyTask task = taskOpt.get();
        if (adjustment.title() != null && !adjustment.title().isBlank()) {
            task.setTitle(adjustment.title());
        }
        if (adjustment.description() != null && !adjustment.description().isBlank()) {
            task.setDescription(adjustment.description());
        }
        if (adjustment.location() != null && !adjustment.location().isBlank()) {
            task.setLocation(adjustment.location());
        }
        if (adjustment.startTime() != null && !adjustment.startTime().isBlank()) {
            task.setStartTime(LocalDateTime.parse(adjustment.startTime().trim(), FLEXIBLE_PARSER));
        }
        if (adjustment.durationMinutes() > 0) {
            task.setDurationMinutes(adjustment.durationMinutes());
        }
        taskRepository.save(task);
        embedTask(task);
        log.info("Task '{}' (ID:{}) aktualisiert für Buddy '{}'", task.getTitle(), task.getId(), buddy.getName());
    }

    private void addTask(TaskAdjustment adjustment, Buddy buddy) {
        VBuddyTask task = new VBuddyTask();
        task.setBuddy(buddy);
        task.setTitle(adjustment.title());
        task.setDescription(adjustment.description());
        task.setLocation(adjustment.location());
        task.setStartTime(LocalDateTime.parse(adjustment.startTime().trim(), FLEXIBLE_PARSER));
        task.setDurationMinutes(adjustment.durationMinutes());
        task.setStatus(TaskStatus.PLANNED);
        taskRepository.save(task);
        embedTask(task);
        log.info("Neuer Task '{}' (ID:{}) angelegt für Buddy '{}'", task.getTitle(), task.getId(), buddy.getName());
    }

    private Optional<VBuddyTask> findPlannedTask(Long taskId, List<VBuddyTask> plannedTasks) {
        if (taskId == null) {
            return Optional.empty();
        }
        return plannedTasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
    }

    private void embedTask(VBuddyTask task) {
        try {
            embeddingService.embedTask(task);
        } catch (Exception e) {
            log.warn("Embedding fehlgeschlagen für Task {} (Buddy {}): {}",
                    task.getId(), task.getBuddy().getName(), e.getMessage());
        }
    }

    private void logDecision(Buddy buddy, PlanAdjustmentAnalysis analysis, String plannedTasksText) {
        String decision = analysis.adjustments().stream()
                .map(a -> String.format("%s — %s (%s)", a.action(), a.title(),
                        a.existingTaskId() != null ? "Task-ID:" + a.existingTaskId() : "neu"))
                .collect(Collectors.joining("\n"));

        AiDecisionLog logEntry = new AiDecisionLog();
        logEntry.setBuddy(buddy);
        logEntry.setContext("Chat-Planänderung | Geplante Tasks:\n" + plannedTasksText);
        logEntry.setDecision(decision);
        logEntry.setReasoning(analysis.reasoning());
        aiDecisionLogRepository.save(logEntry);
    }
}
