package com.sm.authservice.dto;

public record LoginResponseDto(String token, Long expiresIn) {
    public LoginResponseDto {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
        if (expiresIn == null || expiresIn <= 0) {
            throw new IllegalArgumentException("ExpiresIn must be a positive number");
        }
    }
    
} 