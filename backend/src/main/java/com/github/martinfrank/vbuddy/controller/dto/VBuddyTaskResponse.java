package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;

import java.time.LocalDateTime;

public record VBuddyTaskResponse(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime startTime,
        int durationMinutes,
        TaskStatus status,
        LocalDateTime createdAt
) {
    public static VBuddyTaskResponse from(VBuddyTask task) {
        return new VBuddyTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getLocation(),
                task.getStartTime(),
                task.getDurationMinutes(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
