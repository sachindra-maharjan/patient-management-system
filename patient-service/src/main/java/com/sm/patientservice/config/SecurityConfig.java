package com.sm.patientservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sm.patientservice.security.HeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  // This UserDetailsService is a dummy. It's configured to accept user details
    // that are *already authenticated* by the gateway and provided in headers.
    // It does NOT load from a database.
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // This method should ideally not be called directly for loading users from a DB in this setup.
            // Our HeaderAuthenticationFilter will construct UserDetails directly.
            // However, Spring Security sometimes expects a UserDetailsService bean.
            throw new UnsupportedOperationException("This UserDetailsService is for header-based authentication only and does not load from a database.");
        };
    }

    // Defines the security filter chain for HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API service
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // All requests reaching this service are expected to come from the API Gateway
                    // and should thus be authenticated. Public endpoints would be handled at the Gateway.
                    .anyRequest().authenticated()
            )
            // Add our custom filter to extract user info from headers and set security context
            .addFilterBefore(headerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Creates an instance of our custom header authentication filter
    @Bean
    public HeaderAuthenticationFilter headerAuthenticationFilter() {
        // Pass a dummy UserDetailsService if the filter only needs the interface,
        // or refine HeaderAuthenticationFilter to not depend on UserDetailsService directly.
        // For this example, we pass the dummy one.
        return new HeaderAuthenticationFilter(userDetailsService());
    }
  
  
}
