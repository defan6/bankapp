package com.bankapp.accountservice.adapters.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    List<AccountJpaEntity> findAllByUserId(UUID userId);
}
