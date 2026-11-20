package com.wesolve.access;

import com.wesolve.access.api.MemberDirectory;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final MemberDirectory members;
    private final String signInClaim;

    public MeController(MemberDirectory members, @Value("${app.security.signin-claim}") String signInClaim) {
        this.members = members;
        this.signInClaim = signInClaim;
    }

    @GetMapping("/api/v1/me")
    public Map<String, Object> me(JwtAuthenticationToken token) {
        String signIn = token.getToken().getClaimAsString(signInClaim);
        var member = members.requireBySignIn(signIn);
        return Map.of(
                "id",
                member.id(),
                "displayName",
                member.displayName(),
                "actions",
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
