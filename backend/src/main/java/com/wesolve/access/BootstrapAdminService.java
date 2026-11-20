package com.wesolve.access;

import com.wesolve.access.persistence.SupportMemberEntity;
import com.wesolve.access.persistence.SupportMemberRepository;
import com.wesolve.access.persistence.UserEntity;
import com.wesolve.access.persistence.UserRepository;
import com.wesolve.platform.api.SignInHash;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BootstrapAdminService {

    static final UUID ADMIN_GROUP = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3");

    private final String adminSignInId;
    private final String signInClaim;
    private final UserRepository users;
    private final SupportMemberRepository members;
    private final JdbcTemplate jdbc;

    public BootstrapAdminService(
            @Value("${app.bootstrap.admin-sign-in-id:}") String adminSignInId,
            @Value("${app.security.signin-claim}") String signInClaim,
            UserRepository users,
            SupportMemberRepository members,
            JdbcTemplate jdbc) {
        this.adminSignInId = adminSignInId;
        this.signInClaim = signInClaim;
        this.users = users;
        this.members = members;
        this.jdbc = jdbc;
    }

    @Transactional
    public void maybe(Jwt jwt) {
        if (adminSignInId.isBlank()) {
            return;
        }
        String signIn = claim(jwt);
        if (signIn == null || !signIn.equals(adminSignInId.trim().toLowerCase(Locale.ROOT))) {
            return;
        }
        Object verified = jwt.getClaim("email_verified");
        if (!Boolean.TRUE.equals(verified)) {
            return;
        }
        jdbc.execute("SELECT pg_advisory_xact_lock(8675309)");
        Integer admins = jdbc.queryForObject(
                """
                SELECT count(DISTINCT u.id)
                FROM users u
                JOIN user_group ug ON ug.user_hash_key = u.hash_key
                JOIN groups g ON g.id = ug.group_id AND g.active
                JOIN group_role gr ON gr.group_id = g.id
                JOIN role_access ra ON ra.role_id = gr.role_id AND ra.access_code = 'ACCESS_MANAGE'
                WHERE u.active
                """,
                Integer.class);
        if (admins != null && admins > 0) {
            return;
        }
        if (users.findBySignInId(signIn).isPresent()) {
            return;
        }
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            return;
        }
        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = signIn;
        }
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setHashKey(SignInHash.of(signIn));
        user.setSignInId(signIn);
        user.setDisplayName(name);
        user.setEmail(email);
        user.setActive(true);
        user.setCanBeAssigned(true);
        users.saveAndFlush(user);

        SupportMemberEntity member = new SupportMemberEntity();
        member.setId(UUID.randomUUID());
        member.setHashKey(user.getHashKey());
        member.setDisplayName(name);
        member.setAssignable(true);
        members.saveAndFlush(member);

        jdbc.update(
                "INSERT INTO support_member_user (support_member_id, user_hash_key) VALUES (?, ?)",
                member.getId(),
                user.getHashKey());
        jdbc.update(
                "INSERT INTO user_group (user_hash_key, group_id) VALUES (?, ?)", user.getHashKey(), ADMIN_GROUP);
    }

    private String claim(Jwt jwt) {
        String value = jwt.getClaimAsString(signInClaim);
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
