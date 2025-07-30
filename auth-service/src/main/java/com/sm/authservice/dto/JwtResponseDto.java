package com.sm.authservice.dto;

import java.util.Set;

public record JwtResponseDto(String token, String type, Long expiresIn, String email, Set<String> roles) {
    public JwtResponseDto {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }
        if (expiresIn == null || expiresIn <= 0) {
            throw new IllegalArgumentException("ExpiresIn must be a positive number");
        }
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be null or empty");
        }
    }
}
