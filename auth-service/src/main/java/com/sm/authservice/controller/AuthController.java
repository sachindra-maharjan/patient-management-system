package com.sm.authservice.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.sm.authservice.dto.JwtResponseDto;
import com.sm.authservice.dto.LoginRequestDto;
import com.sm.authservice.dto.TokenValidationRequest;
import com.sm.authservice.dto.TokenValidationResponse;
import com.sm.authservice.security.jwt.JwtTokenProvider;
import com.sm.authservice.service.CustomUserDetailsService;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
        CustomUserDetailsService userDetailsService,
        JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {

        // Authenticate user credentials with Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.email(), 
                loginRequest.password()));

        /** 
         * Set the authentication object in SecurityContextHolder for the current request context (optional but good practice) 
         * After successful authentication, storing it makes the user information available for:
            Subsequent authorization checks
            Accessing user details in other controllers/services
            Spring Security's @PreAuthorize/@Secured annotations
            Audit logging
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var token = jwtTokenProvider.generateToken(authentication);
        if (token != null) {
            log.info("User {} logged in successfully", loginRequest.email());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Set<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
            
            return ResponseEntity.ok(new JwtResponseDto(token.token(), "Bearer", token.expiresIn(), userDetails.getUsername(), roles));
        } else {
            log.error("Login failed for user {}", loginRequest.email());
            throw new JwtException("Login failed for user " + loginRequest.email());
        }        
    }

    @Operation(summary = "Validate JWT token", description = "Validate the provided JWT token")
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtException("Invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (jwtTokenProvider.validateToken(token)) {
            var username = jwtTokenProvider.getUserFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            var roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
            return ResponseEntity.ok(new TokenValidationResponse(true, userDetails.getUsername(), roles, "Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new TokenValidationResponse(false, null, Set.of(), "Token is invalid or expired"));
        }
    }

    /**
     * Handles user logout. For stateless JWTs, this typically means client-side token discarding.
     * If using token blacklisting or refresh tokens, implement server-side invalidation here.
     * Accessible to unauthenticated callers (gateway/client).
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenValidationRequest request) {
        // In a true stateless JWT system, logout is client-side: discard the token.
        // For more advanced scenarios (e.g., refresh tokens, blacklisting):
        //   - Implement a mechanism to invalidate refresh tokens.
        //   - Implement a JWT blacklist/revocation list if immediate token invalidation is critical (adds state).
        log.info("Logout request received for token (client should discard): {}", request.token());
        return ResponseEntity.ok("Logged out successfully. Please discard your token on the client side.");
    }

}
