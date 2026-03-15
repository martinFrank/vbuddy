package com.github.martinfrank.vbuddy.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String content
) {}
