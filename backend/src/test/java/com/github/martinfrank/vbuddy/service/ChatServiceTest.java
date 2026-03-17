package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.ChatMessageRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private BuddyService buddyService;
    @Mock
    private VBuddyTaskRepository taskRepository;
    @Mock
    private ChatLanguageModel chatChatModel;
    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void getHistory_delegatesToRepository() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Hallo");
        when(chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(msg));

        List<ChatMessage> result = chatService.getHistory(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void sendMessage_savesUserAndAssistantMessages() {
        Buddy buddy = createBuddy();
        when(buddyService.findById(1L)).thenReturn(buddy);
        when(buddyService.getNeeds(1L)).thenReturn(List.of());
        when(chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(embeddingService.retrieveRelevantContext(anyLong(), anyString(), anyInt())).thenReturn(List.of());

        ChatResponse chatResponse = mock(ChatResponse.class);
        AiMessage aiMessage = new AiMessage("Hallo! Ich bin Max.");
        when(chatResponse.aiMessage()).thenReturn(aiMessage);
        when(chatChatModel.chat(any(ChatRequest.class))).thenReturn(chatResponse);

        ChatMessage result = chatService.sendMessage(1L, "Hi!");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, times(2)).save(captor.capture());

        List<ChatMessage> saved = captor.getAllValues();
        assertThat(saved.get(0).getRole()).isEqualTo(MessageRole.USER);
        assertThat(saved.get(0).getContent()).isEqualTo("Hi!");
        assertThat(saved.get(1).getRole()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(saved.get(1).getContent()).isEqualTo("Hallo! Ich bin Max.");
    }

    @Test
    void sendMessage_llmFailure_returnsFallbackMessage() {
        Buddy buddy = createBuddy();
        when(buddyService.findById(1L)).thenReturn(buddy);
        when(buddyService.getNeeds(1L)).thenReturn(List.of());
        when(chatMessageRepository.findByBuddyIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(embeddingService.retrieveRelevantContext(anyLong(), anyString(), anyInt())).thenReturn(List.of());
        when(chatChatModel.chat(any(ChatRequest.class))).thenThrow(new RuntimeException("LLM down"));

        ChatMessage result = chatService.sendMessage(1L, "Hi!");

        assertThat(result.getContent()).contains("Entschuldigung");
        assertThat(result.getRole()).isEqualTo(MessageRole.ASSISTANT);
    }

    private Buddy createBuddy() {
        Buddy buddy = new Buddy();
        buddy.setId(1L);
        buddy.setName("Max");
        buddy.setPersonality("freundlich und hilfsbereit");
        buddy.setCurrentLocation("Zu Hause");
        return buddy;
    }
}
