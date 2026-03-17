package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.BackgroundStatus;
import com.github.martinfrank.vbuddy.model.BuddyBackground;

import java.time.LocalDateTime;

public record BuddyBackgroundResponse(
        Long id,
        String structuredData,
        String narrativeText,
        BackgroundStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BuddyBackgroundResponse from(BuddyBackground bg) {
        return new BuddyBackgroundResponse(
                bg.getId(),
                bg.getStructuredData(),
                bg.getNarrativeText(),
                bg.getStatus(),
                bg.getCreatedAt(),
                bg.getUpdatedAt()
        );
    }
}
