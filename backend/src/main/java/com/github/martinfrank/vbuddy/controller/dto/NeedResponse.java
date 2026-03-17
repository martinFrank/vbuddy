package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.Need;
import com.github.martinfrank.vbuddy.model.NeedType;

import java.time.LocalDateTime;

public record NeedResponse(
        Long id,
        NeedType needType,
        double currentValue,
        double maxValue,
        double decayRatePerHour,
        LocalDateTime updatedAt
) {
    public static NeedResponse from(Need need) {
        return new NeedResponse(
                need.getId(),
                need.getNeedType(),
                need.getCurrentValue(),
                need.getMaxValue(),
                need.getDecayRatePerHour(),
                need.getUpdatedAt()
        );
    }
}
