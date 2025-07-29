package com.sm.authservice.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sm.authservice.dto.JwtToken;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private final Key secretKey;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret, 
                   @Value("${jwt.expiration:3600000}") long expiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration; // Default to 1 hour if not specified
    }

    public JwtToken generateToken(String email, String roles) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (roles == null || roles.isEmpty()) {
            roles = "USER"; // Default role if none provided
        }

        var expiresIn = new Date(System.currentTimeMillis() + this.expiration).getTime();
        var token = io.jsonwebtoken.Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + this.expiration))
                .signWith(secretKey)
                .compact();
        return new JwtToken(token, expiresIn);
    }

    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey)secretKey)
                .build()
                .parseSignedClaims(token);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }

    public Long tokenExpiresIn(String token) {
        try {
            return Jwts.parser().verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .getTime();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }

}
