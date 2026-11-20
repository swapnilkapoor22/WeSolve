package com.wesolve.platform.api;

import org.springframework.security.oauth2.jwt.Jwt;

public interface RequestGate {

  void check(Jwt jwt);
}
