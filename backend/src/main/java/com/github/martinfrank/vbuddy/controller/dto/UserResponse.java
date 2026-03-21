package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.AppUser;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String role, LocalDateTime createdAt) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getCreatedAt());
    }
}
