package com.sm.apigateway.filter;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.sm.apigateway.client.AuthServiceClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object>{

  private final AuthServiceClient authServiceClient;

  public JwtValidationGatewayFilterFactory(AuthServiceClient authServiceClient) {
    this.authServiceClient = authServiceClient;
  }

  /**
   * Strip all X- headers from incoming request
      1. Strip all X- headers from incoming request
      2. Validate JWT token
      3. If token is valid:
          Add back only the verified user information in X- headers
          Forward the request with clean headers
      4. If token is invalid:
          Reject the request
   */
  @Override
  public GatewayFilter apply(Object config) {

    return (exchange, chain) -> {
      // Strip all X- headers from the incoming request
      var headers = exchange.getRequest().getHeaders();
      var mutatedRequest = exchange.getRequest().mutate();
      
      headers.forEach((key, values) -> {
        if (key.startsWith("X-")) {
          mutatedRequest.headers(httpHeaders -> httpHeaders.remove(key));
        }
      });
      
      String jwtToken = headers.getFirst("Authorization");

      if (jwtToken == null || !jwtToken.startsWith("Bearer ")) {
        return exchange.getResponse().setComplete();
      }
      
      return authServiceClient.validateToken(jwtToken)
          .flatMap(response -> {
            log.debug("JWT Validation Response: {}", response);
            if(response.valid()) {
              // Add validated user information in new headers
              mutatedRequest
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .header("X-User-Email", response.email())
                  .header("X-User-Roles", String.join(",", response.roles()));
              
              // Create new exchange with cleaned headers
              var newExchange = exchange.mutate()
                  .request(mutatedRequest.build())
                  .build();
              
              return chain.filter(newExchange);
            } else {
              return exchange.getResponse().setComplete();
            }
        });  
    };
  
  }
}