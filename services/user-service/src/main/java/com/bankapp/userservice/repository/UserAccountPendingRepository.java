package com.bankapp.userservice.repository;

import com.bankapp.userservice.domain.UserAccountPending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountPendingRepository extends JpaRepository<UserAccountPending, UUID> {

    Optional<UserAccountPending> findByUserId(UUID userId);

    List<UserAccountPending> findByCreatedAtBefore(LocalDateTime cutoffTime);

    boolean existsByUserId(UUID userId);

    void deleteByUserIdIn(List<UUID> userIds);
}
