package com.sm.authservice.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.sm.authservice.dto.JwtToken;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
    
    private final Key secretKey;
    private final long expiration; // in milliseconds

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, 
                   @Value("${jwt.expiration:3600000}") long expiration) {
        this.secretKey = getSecretKey(secret);
        this.expiration = expiration; // Default to 1 hour if not specified
    }

    public Key getSecretKey(String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for the given authentication.
     * @param authentication   the authentication object containing user details
     * @return the generated JWT token as a String
     */
    public JwtToken generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new IllegalArgumentException("User details or username cannot be null");
        }

        String roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        
        var expiryDate = new java.util.Date(System.currentTimeMillis() + this.expiration);
        String token = Jwts.builder()
                            .subject(userDetails.getUsername())
                            .claim("roles", roles)
                            .issuedAt(new java.util.Date())
                            .expiration(expiryDate)
                            .signWith(secretKey)
                            .compact();

        return new JwtToken(token, expiryDate.getTime());
    }

    /**
     * Validates the given JWT token.
     * @param token the JWT token to validate
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtException("JWT token is empty or null");
        }

        try {
            Jwts.parser().verifyWith((SecretKey)secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token", e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT token is empty or null", e);
        } catch (JwtException e) {
            log.error("Invalid JWT token", e);
        }
        return false;
    }

    public String getUserFromToken(String token) {
        if(token == null || token.isEmpty()) {
            throw new JwtException("JWT token is empty or null");
        }
        try {
            return Jwts.parser().verifyWith((SecretKey)secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }

}
