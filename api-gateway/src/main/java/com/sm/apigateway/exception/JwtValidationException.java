package com.sm.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class JwtValidationException {
  
  @ExceptionHandler(WebClientResponseException.Unauthorized.class)
  public Mono<Void> handleJwtValidationException(ServerWebExchange exchange) {
    log.error("JWT validation failed: {}", exchange.getResponse().getStatusCode());
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

}
