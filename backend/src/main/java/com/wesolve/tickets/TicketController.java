package com.wesolve.tickets;

import com.wesolve.access.api.Member;
import com.wesolve.access.api.MemberDirectory;
import com.wesolve.tickets.persistence.TicketEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

  private final CreateTicketService tickets;
  private final MemberDirectory members;

  public TicketController(CreateTicketService tickets, MemberDirectory members) {
    this.tickets = tickets;
    this.members = members;
  }

  @PostMapping("/api/v1/tickets")
  @PreAuthorize("hasAuthority('TICKET_CREATE')")
  public ResponseEntity<Map<String, Object>> create(
      @Valid @RequestBody CreateTicketRequest request, JwtAuthenticationToken token) {
    Member actor = members.requireBySignIn(token.getToken().getClaimAsString("preferred_username"));
    TicketEntity ticket =
        tickets.create(
            request.title(),
            request.description(),
            request.priority(),
            request.assigneeId(),
            actor.id());
    Member assignee = members.findAssignable(ticket.getAssigneeId()).orElseThrow();
    return ResponseEntity.created(URI.create("/api/v1/tickets/" + ticket.getTicketNumber()))
        .body(
            Map.of(
                "number",
                ticket.getTicketNumber(),
                "title",
                ticket.getTitle(),
                "description",
                ticket.getDescription(),
                "priority",
                ticket.getPriority(),
                "status",
                ticket.getStatus(),
                "assignee",
                Map.of("id", assignee.id(), "displayName", assignee.displayName()),
                "createdAt",
                ticket.getCreatedAt(),
                "updatedAt",
                ticket.getUpdatedAt()));
  }

  public record CreateTicketRequest(
      @NotBlank @Size(max = 150) String title,
      @NotBlank @Size(max = 5000) String description,
      @NotNull @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT") String priority,
      @NotNull UUID assigneeId) {}
}
