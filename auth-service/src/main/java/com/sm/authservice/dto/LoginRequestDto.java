package com.sm.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank(message = "Email is required") @Email(message = "Email should be valid email address") @JsonProperty("email") String email, 
    @NotBlank(message = "Password is required") @JsonProperty("password") String password) {
}