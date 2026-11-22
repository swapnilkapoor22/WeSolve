package com.wesolve.platform.security;

import com.wesolve.platform.api.ApiException;
import com.wesolve.platform.api.RequestGate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<RequestGate> gates)
      throws Exception {
    OncePerRequestFilter gateFilter =
        new OncePerRequestFilter() {
          @Override
          protected void doFilterInternal(
              jakarta.servlet.http.HttpServletRequest request,
              jakarta.servlet.http.HttpServletResponse response,
              jakarta.servlet.FilterChain chain)
              throws jakarta.servlet.ServletException, java.io.IOException {
            var authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication();
            if (authentication != null
                && authentication.getPrincipal()
                    instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
              try {
                gates.forEach(gate -> gate.check(jwt));
              } catch (ApiException ex) {
                response.setStatus(ex.status());
                response.setContentType("application/problem+json");
                response
                    .getWriter()
                    .write(
                        "{\"title\":\"Forbidden\",\"status\":403,\"code\":\"NO_ACCESS\",\"traceId\":\"none\",\"detail\":\""
                            + ex.getMessage().replace("\"", "")
                            + "\"}");
                return;
              }
            }
            chain.doFilter(request, response);
          }
        };
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/public-config")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
        .exceptionHandling(
            errors ->
                errors.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      response.setContentType("application/problem+json");
                      response
                          .getWriter()
                          .write(
                              "{\"title\":\"Unauthorized\",\"status\":401,\"code\":\"NO_ACCESS\",\"traceId\":\"none\"}");
                    }))
        .addFilterAfter(gateFilter, BearerTokenAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  @Profile("!test")
  org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
    return org.springframework.security.oauth2.jwt.JwtDecoders.fromIssuerLocation(issuer);
  }
}
