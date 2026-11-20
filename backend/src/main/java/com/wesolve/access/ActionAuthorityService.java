package com.wesolve.access;

import com.wesolve.platform.api.SignInHash;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActionAuthorityService {

  private final JdbcTemplate jdbc;

  public ActionAuthorityService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<String> actionsForSignIn(String signInId) {
    return jdbc.query(
        """
                SELECT DISTINCT ra.access_code
                FROM users u
                JOIN user_group ug ON ug.user_hash_key = u.hash_key
                JOIN groups g ON g.id = ug.group_id AND g.active
                JOIN group_role gr ON gr.group_id = g.id
                JOIN role_access ra ON ra.role_id = gr.role_id
                WHERE u.hash_key = ? AND u.active
                ORDER BY ra.access_code
                """,
        (rs, row) -> rs.getString(1),
        SignInHash.of(signInId));
  }
}
