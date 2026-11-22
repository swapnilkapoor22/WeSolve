package com.wesolve.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Test-only decoder. It lives in test sources and is active only under the "test" profile, so no
 * production build can accept an unsigned token.
 */
@TestConfiguration
public class TestJwtConfig {

  @Bean
  JwtDecoder testJwtDecoder() {
    return token ->
        Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("preferred_username", "admin")
            .claim("email", "admin@example.com")
            .claim("email_verified", true)
            .build();
  }
}
