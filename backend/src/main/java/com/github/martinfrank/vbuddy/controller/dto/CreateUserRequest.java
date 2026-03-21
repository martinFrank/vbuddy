package com.github.martinfrank.vbuddy.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Pattern(regexp = "ADMIN|USER") String role
) {}
