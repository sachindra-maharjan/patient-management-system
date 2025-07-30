package com.sm.apigateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.sm.apigateway.dto.TokenValidationResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder
            .baseUrl(authServiceUrl)
            .build();
    }

    public Mono<TokenValidationResponse> validateToken(String jwtToken) {
        return webClient.get()
            .uri("/validate")
            .header(HttpHeaders.AUTHORIZATION, jwtToken)
            .retrieve()
            .bodyToMono(TokenValidationResponse.class)
            .onErrorResume(e -> {
                // Handle error, e.g., log it or return a default response
                log.error("Token validation failed", e);
                return Mono.just(new TokenValidationResponse(false, "Invalid token"));
            });
    }

}
