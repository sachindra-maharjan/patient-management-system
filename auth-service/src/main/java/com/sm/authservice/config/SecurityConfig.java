package com.sm.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.sm.authservice.security.jwt.AuthEntryPointJwt;
import com.sm.authservice.security.jwt.JwtTokenProvider;
import com.sm.authservice.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final AuthEntryPointJwt authEntryPointJwt;


    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtTokenProvider jwtTokenProvider,
                          AuthEntryPointJwt authEntryPointJwt) {
        this.userDetailsService = customUserDetailsService;
        this.authEntryPointJwt = authEntryPointJwt;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public DaoAuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    //     provider.setPasswordEncoder(passwordEncoder());
    //     return provider;
    // }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authEntryPointJwt)) // Use custom entry point for handling authentication errors
            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless session management
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/validate", "/logout").permitAll() // Allow public access to login and register endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow public access to Swagger UI and API docs
                .requestMatchers("/actuator/**").permitAll() // Allow public access to Actuator endpoints
                .requestMatchers("/h2-console/**").permitAll() // Secure all other API endpoints
                .anyRequest().authenticated())
            ;
        
        // For H2 console frame to work (if CSRF is disabled), needed if you keep H2 console exposed
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

}
