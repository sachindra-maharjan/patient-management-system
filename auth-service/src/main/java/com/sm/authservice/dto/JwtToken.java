package com.sm.authservice.dto;

public record JwtToken(String token, Long expiresIn) {
    public JwtToken {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (expiresIn == null || expiresIn <= 0) {
            throw new IllegalArgumentException("ExpiresIn must be a positive number");
        }
    }
}
