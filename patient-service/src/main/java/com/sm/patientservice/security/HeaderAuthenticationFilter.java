package com.sm.patientservice.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService; //Used for contract, but not for DB loading

  public HeaderAuthenticationFilter(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Extract user information from headers injected by the API Gateway
    String username = request.getHeader("X-AUTH-USER-EMAIL");
    String rolesHeader = request.getHeader("X-AUTH-USER-ROLES");

    // If authentication headers are present, create an Authentication object
    if (username != null && rolesHeader != null) {
      List<GrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());

      // Create a UserDetails object using the extracted information.
      // The password field is not relevant here as authentication already occurred at the gateway.
      UserDetails userDetails = new User(username, "[PROTECTED]", authorities);

      // Create an Authentication token and set it in the SecurityContextHolder
      // This makes the user's details and authorities available for @PreAuthorize, etc.
      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      // Set details from the request (IP, session ID if any, though sessions are stateless)
      authentication.setDetails(userDetails); // Can also set new WebAuthenticationDetailsSource().buildDetails(request)

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Continue the filter chain
    filterChain.doFilter(request, response);

  }

}
