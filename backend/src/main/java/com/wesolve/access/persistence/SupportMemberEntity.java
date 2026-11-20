package com.wesolve.access.persistence;

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

@Entity
@Table(name = "support_member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportMemberEntity {

  @Id private UUID id;

  @Column(name = "hash_key", nullable = false, updatable = false)
  private String hashKey;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(nullable = false)
  private boolean assignable;

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

  public SupportMemberEntity(UUID id, String hashKey, String displayName, boolean assignable) {
    this.id = id;
    this.hashKey = hashKey;
    this.displayName = displayName;
    this.assignable = assignable;
  }
}
