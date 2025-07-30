package com.sm.authservice.dto;

import java.util.Set;

public record TokenValidationResponse(boolean valid,
    String email,
    Set<String> roles,
    String message) {
}
