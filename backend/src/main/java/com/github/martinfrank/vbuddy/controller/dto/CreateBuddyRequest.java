package com.github.martinfrank.vbuddy.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBuddyRequest(
        @NotBlank String name,
        @NotBlank String personality
) {}
