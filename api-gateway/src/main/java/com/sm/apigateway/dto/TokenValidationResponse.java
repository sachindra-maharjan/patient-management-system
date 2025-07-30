package com.sm.apigateway.dto;

import java.util.Set;

public record TokenValidationResponse(boolean valid,
    String email,
    Set<String> roles,
    String message) {

    public TokenValidationResponse(boolean valid, String message) {
        this(valid, null, Set.of(), message);
    }
}
