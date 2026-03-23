package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, UtcDateTimeUtil.now());
    }
}
