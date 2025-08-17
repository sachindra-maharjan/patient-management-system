package com.sm.authservice.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sm.authservice.dto.ErrorResponseDto;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private record ErrorResponseBuilder(HttpStatus status, String errorCode, String message) {
        private static String currentTimestamp() {
            return java.time.Instant.now().toString();
        }

        ErrorResponseDto build() {
            return new ErrorResponseDto(status.value(), errorCode, message, currentTimestamp());
        }

        ErrorResponseDto build(List<ErrorResponseDto.FieldErrorResponseDto> fieldErrors) {
            return new ErrorResponseDto(status.value(), errorCode, message, currentTimestamp(), fieldErrors);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponseDto.FieldErrorResponseDto(
                    fieldError.getField(), 
                    fieldError.getDefaultMessage()
                ))
                .toList();

        return createErrorResponse(new ErrorResponseBuilder(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Validation failed for one or more fields"
        ), fieldErrors);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return createErrorResponse(new ErrorResponseBuilder(
            HttpStatus.NOT_FOUND,
            "NOT_FOUND",
            "User not found"
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(BadCredentialsException ex) {
        log.error("Bad credentials provided", ex.getMessage());
        return createErrorResponse(new ErrorResponseBuilder(
            HttpStatus.UNAUTHORIZED,
            "BAD_CREDENTIALS",
            "Login failed due to invalid credentials"
        ));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtException(JwtException ex) {
        log.error("JWT error occurred: {}", ex.getMessage());
        return createErrorResponse(new ErrorResponseBuilder(
            HttpStatus.FORBIDDEN,
            "UNAUTHORIZED",
            "Invalid JWT token"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return createErrorResponse(new ErrorResponseBuilder(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred"
        ));
    }

    private ResponseEntity<ErrorResponseDto> createErrorResponse(ErrorResponseBuilder builder) {
        return ResponseEntity
            .status(builder.status())
            .body(builder.build());
    }

    private ResponseEntity<ErrorResponseDto> createErrorResponse(
            ErrorResponseBuilder builder,
            List<ErrorResponseDto.FieldErrorResponseDto> fieldErrors) {
        return ResponseEntity
            .status(builder.status())
            .body(builder.build(fieldErrors));
    }
}
