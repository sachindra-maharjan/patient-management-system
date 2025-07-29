package com.sm.authservice.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sm.authservice.dto.JwtToken;
import com.sm.authservice.dto.LoginRequestDto;
import com.sm.authservice.repository.UserRepository;
import com.sm.authservice.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(UserRepository userRepository, 
    PasswordEncoder passwordEncoder,
    JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public Optional<JwtToken> authenticate(LoginRequestDto loginRequest) {
    if (loginRequest == null || 
        loginRequest.email() == null || 
        loginRequest.password() == null) {
      return Optional.empty();
    }

    return userRepository.findByEmail(loginRequest.email())
        .filter(user -> passwordEncoder.matches(loginRequest.password(), user.getPassword()))
        .map(user -> jwtUtil.generateToken(user.getEmail(), user.getRoles()));
  }

  public boolean validateToken(String token) {
    if (token == null || token.isEmpty()) {
      return false; // Token is invalid, return false
    }

    try{
       jwtUtil.validateToken(token);
    } catch(JwtException e){
      log.error("JWT validation failed: {}", e.getMessage());
      return false; // Token is invalid, return false
    }
    return true; // Token is valid
  }
    
}
