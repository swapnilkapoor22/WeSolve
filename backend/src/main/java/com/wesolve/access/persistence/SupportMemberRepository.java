package com.wesolve.access.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMemberRepository extends JpaRepository<SupportMemberEntity, UUID> {

    Optional<SupportMemberEntity> findByHashKey(String hashKey);

    Optional<SupportMemberEntity> findByIdAndAssignableTrue(UUID id);
}
