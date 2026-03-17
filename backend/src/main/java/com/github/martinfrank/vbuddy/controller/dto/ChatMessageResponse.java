package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.ChatMessage;
import com.github.martinfrank.vbuddy.model.MessageRole;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        MessageRole role,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
