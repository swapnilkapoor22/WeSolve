package com.wesolve.access;

import com.wesolve.access.persistence.UserRepository;
import com.wesolve.platform.api.ApiException;
import com.wesolve.platform.api.RequestGate;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AccessRequestGate implements RequestGate {

  private final BootstrapAdminService bootstrap;
  private final UserRepository users;
  private final ActionAuthorityService actions;
  private final String signInClaim;

  public AccessRequestGate(
      BootstrapAdminService bootstrap,
      UserRepository users,
      ActionAuthorityService actions,
      @Value("${app.security.signin-claim}") String signInClaim) {
    this.bootstrap = bootstrap;
    this.users = users;
    this.actions = actions;
    this.signInClaim = signInClaim;
  }

  @Override
  public void check(Jwt jwt) {
    bootstrap.maybe(jwt);
    String signIn = jwt.getClaimAsString(signInClaim);
    if (signIn == null || signIn.isBlank()) {
      throw new ApiException(403, "NO_ACCESS", "No access");
    }
    String normalized = signIn.trim().toLowerCase(Locale.ROOT);
    var user =
        users
            .findBySignInId(normalized)
            .orElseThrow(() -> new ApiException(403, "NO_ACCESS", "No access"));
    if (!user.isActive()) {
      throw new ApiException(403, "NO_ACCESS", "No access");
    }
    var authorities =
        actions.actionsForSignIn(normalized).stream().map(SimpleGrantedAuthority::new).toList();
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
  }
}
