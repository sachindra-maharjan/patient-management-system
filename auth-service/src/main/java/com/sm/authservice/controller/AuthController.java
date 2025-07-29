package com.sm.authservice.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.sm.authservice.dto.JwtToken;
import com.sm.authservice.dto.LoginRequestDto;
import com.sm.authservice.dto.LoginResponseDto;
import com.sm.authservice.service.AuthService;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        Optional<JwtToken> token = authService.authenticate(loginRequest);
        JwtToken jwtToken = token.orElseThrow(() -> new JwtException("Authentication failed"));
        return ResponseEntity.ok(new LoginResponseDto(jwtToken.token(), jwtToken.expiresIn()));
    }

    @Operation(summary = "Validate JWT token", description = "Validate the provided JWT token")
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtException("Invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return authService.validateToken(token) ? 
            ResponseEntity.ok().build() :
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
