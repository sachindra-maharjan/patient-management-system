package com.sm.authservice.dto;

import java.util.List;

public record ErrorResponseDto(int code, String status, String message, String timestamp, List<FieldErrorResponseDto> fieldErrors) {
    private static final List<FieldErrorResponseDto> EMPTY_FIELD_ERRORS = List.of();

    public ErrorResponseDto(int code, String status, String message, String timestamp) {
        this(code, status, message, java.time.Instant.now().toString(), EMPTY_FIELD_ERRORS);
    }

    public ErrorResponseDto(String status, String message) {
        this(400, status, message, java.time.Instant.now().toString(), EMPTY_FIELD_ERRORS);
    }

    public record FieldErrorResponseDto(String field, String message) {
        public FieldErrorResponseDto(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
