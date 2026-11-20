package com.wesolve.tickets;

import com.wesolve.access.api.MemberDirectory;
import com.wesolve.platform.api.ApiException;
import com.wesolve.tickets.persistence.TicketEntity;
import com.wesolve.tickets.persistence.TicketRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTicketService {

    private final TicketRepository tickets;
    private final MemberDirectory members;
    private final JdbcTemplate jdbc;
    private final EntityManager entities;

    public CreateTicketService(
            TicketRepository tickets, MemberDirectory members, JdbcTemplate jdbc, EntityManager entities) {
        this.tickets = tickets;
        this.members = members;
        this.jdbc = jdbc;
        this.entities = entities;
    }

    @Transactional
    public TicketEntity create(String title, String description, String priority, UUID assigneeId, UUID actorMemberId) {
        if (members.findAssignable(assigneeId).isEmpty()) {
            throw new ApiException(422, "VALIDATION_FAILED", "Assignee cannot be assigned tickets.");
        }
        TicketEntity ticket = new TicketEntity();
        ticket.setId(UUID.randomUUID());
        ticket.setTitle(title.trim());
        ticket.setDescription(description.trim());
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");
        ticket.setAssigneeId(assigneeId);
        tickets.saveAndFlush(ticket);
        entities.refresh(ticket);
        jdbc.update(
                """
                INSERT INTO ticket_history (ticket_id, actor_id, to_status, to_assignee_id)
                VALUES (?, ?, 'OPEN', ?)
                """,
                ticket.getId(),
                actorMemberId,
                assigneeId);
        return ticket;
    }
}
