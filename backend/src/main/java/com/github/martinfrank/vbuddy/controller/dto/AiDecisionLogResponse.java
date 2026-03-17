package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.AiDecisionLog;

import java.time.LocalDateTime;

public record AiDecisionLogResponse(
        Long id,
        String context,
        String decision,
        String reasoning,
        LocalDateTime createdAt
) {
    public static AiDecisionLogResponse from(AiDecisionLog log) {
        return new AiDecisionLogResponse(
                log.getId(),
                log.getContext(),
                log.getDecision(),
                log.getReasoning(),
                log.getCreatedAt()
        );
    }
}
