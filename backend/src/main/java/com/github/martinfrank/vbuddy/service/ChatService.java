package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.ChatMessage;
import com.github.martinfrank.vbuddy.model.MessageRole;
import com.github.martinfrank.vbuddy.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final BuddyService buddyService;

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

        // TODO: LLM-Integration — Antwort generieren basierend auf Persönlichkeit, Bedürfnissen, Tagesplan
        String response = "Hallo! Ich bin " + buddy.getName() + ". Die LLM-Integration kommt bald!";

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setBuddy(buddy);
        assistantMsg.setRole(MessageRole.ASSISTANT);
        assistantMsg.setContent(response);
        chatMessageRepository.save(assistantMsg);

        return assistantMsg;
    }
}
