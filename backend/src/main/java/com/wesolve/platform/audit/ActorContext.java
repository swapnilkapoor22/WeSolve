package com.wesolve.platform.audit;

import com.wesolve.platform.api.SignInHash;
import java.util.Locale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Order(10)
public class ActorContext {

    private final JdbcTemplate jdbc;
    private final String signInClaim;

    public ActorContext(JdbcTemplate jdbc, @Value("${app.security.signin-claim}") String signInClaim) {
        this.jdbc = jdbc;
        this.signInClaim = signInClaim;
    }

    @Around("execution(* com.wesolve.access..*(..)) || execution(* com.wesolve.tickets..*(..))")
    public Object setActor(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return joinPoint.proceed();
        }
        jdbc.queryForObject("SELECT set_config('app.actor', ?, true)", String.class, actor());
        return joinPoint.proceed();
    }

    private String actor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            String signIn = token.getToken().getClaimAsString(signInClaim);
            if (signIn != null && !signIn.isBlank()) {
                return SignInHash.of(signIn.trim().toLowerCase(Locale.ROOT));
            }
        }
        return "system";
    }
}
