package com.wesolve.tickets.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketEntity {

  @Id private UUID id;

  @Generated(event = EventType.INSERT)
  @Column(name = "ticket_number", insertable = false, updatable = false)
  private Long ticketNumber;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private String priority;

  @Column(nullable = false)
  private String status;

  @Column(name = "assignee_id", nullable = false)
  private UUID assigneeId;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "last_watermark")
  private Long lastWatermark;

  public TicketEntity(
      UUID id, String title, String description, String priority, String status, UUID assigneeId) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.status = status;
    this.assigneeId = assigneeId;
  }
}
