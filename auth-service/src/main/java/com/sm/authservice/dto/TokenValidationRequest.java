package com.sm.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenValidationRequest(@NotBlank String token) {
    public TokenValidationRequest {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
    }
}
