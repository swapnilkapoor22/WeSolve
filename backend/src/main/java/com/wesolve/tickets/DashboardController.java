package com.wesolve.tickets;

import com.wesolve.access.api.MemberDirectory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

  private final MemberDirectory members;
  private final JdbcTemplate jdbc;

  public DashboardController(MemberDirectory members, JdbcTemplate jdbc) {
    this.members = members;
    this.jdbc = jdbc;
  }

  @GetMapping("/api/v1/dashboard/summary")
  public Map<String, Long> summary(JwtAuthenticationToken token) {
    var member = members.requireBySignIn(token.getToken().getClaimAsString("preferred_username"));
    Map<String, Long> counts = new LinkedHashMap<>();
    counts.put("OPEN", 0L);
    counts.put("IN_PROGRESS", 0L);
    counts.put("RESOLVED", 0L);
    counts.put("CLOSED", 0L);
    counts.put("CANCELLED", 0L);
    jdbc.query(
        "SELECT status, count(*) FROM ticket WHERE assignee_id = ? GROUP BY status",
        rs -> {
          counts.put(rs.getString(1), rs.getLong(2));
        },
        member.id());
    long all = counts.values().stream().mapToLong(Long::longValue).sum();
    return Map.of(
        "all", all,
        "open", counts.get("OPEN"),
        "inProgress", counts.get("IN_PROGRESS"),
        "resolved", counts.get("RESOLVED"),
        "closed", counts.get("CLOSED"),
        "cancelled", counts.get("CANCELLED"));
  }

  @GetMapping("/api/v1/dashboard/tickets")
  public Map<String, Object> tickets(
      JwtAuthenticationToken token,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    int bounded = Math.min(Math.max(size, 1), 100);
    int safePage = Math.max(page, 0);
    var member = members.requireBySignIn(token.getToken().getClaimAsString("preferred_username"));
    Long total =
        jdbc.queryForObject(
            "SELECT count(*) FROM ticket WHERE assignee_id = ?", Long.class, member.id());
    List<Map<String, Object>> items =
        jdbc.query(
            """
                SELECT t.ticket_number, t.title, t.priority, t.status, t.created_at, m.id, m.display_name
                FROM ticket t
                JOIN support_member m ON m.id = t.assignee_id
                WHERE t.assignee_id = ?
                ORDER BY t.ticket_number DESC
                LIMIT ? OFFSET ?
                """,
            (rs, row) ->
                Map.of(
                    "number", rs.getLong(1),
                    "title", rs.getString(2),
                    "priority", rs.getString(3),
                    "status", rs.getString(4),
                    "createdAt", rs.getTimestamp(5).toInstant(),
                    "assignee", Map.of("id", rs.getObject(6), "displayName", rs.getString(7))),
            member.id(),
            bounded,
            safePage * bounded);
    long totalItems = total == null ? 0 : total;
    long totalPages = totalItems == 0 ? 0 : (totalItems + bounded - 1) / bounded;
    return Map.of(
        "items",
        items,
        "page",
        safePage,
        "size",
        bounded,
        "totalItems",
        totalItems,
        "totalPages",
        totalPages);
  }
}
