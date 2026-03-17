package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.Activity;
import com.github.martinfrank.vbuddy.model.ActivityStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ActivityResponse(
        Long id,
        String title,
        String description,
        LocalTime startTime,
        LocalTime endTime,
        ActivityStatus status,
        LocalDateTime createdAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getStatus(),
                activity.getCreatedAt()
        );
    }
}
