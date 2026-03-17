package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.DailyPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailyPlanResponse(
        Long id,
        LocalDate planDate,
        List<ActivityResponse> activities,
        LocalDateTime createdAt
) {
    public static DailyPlanResponse from(DailyPlan plan) {
        return new DailyPlanResponse(
                plan.getId(),
                plan.getPlanDate(),
                plan.getActivities().stream().map(ActivityResponse::from).toList(),
                plan.getCreatedAt()
        );
    }
}
