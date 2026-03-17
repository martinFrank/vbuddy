package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.Buddy;

import java.time.LocalDateTime;

public record BuddyResponse(
        Long id,
        String name,
        String personality,
        String currentLocation,
        LocalDateTime createdAt
) {
    public static BuddyResponse from(Buddy buddy) {
        return new BuddyResponse(
                buddy.getId(),
                buddy.getName(),
                buddy.getPersonality(),
                buddy.getCurrentLocation(),
                buddy.getCreatedAt()
        );
    }
}
