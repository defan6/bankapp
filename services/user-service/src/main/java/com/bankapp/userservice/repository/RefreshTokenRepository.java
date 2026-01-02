package com.bankapp.userservice.repository;

import com.bankapp.userservice.domain.RefreshToken;
import com.bankapp.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    boolean existsByToken(String token);

    Optional<RefreshToken> findByUser_Id(UUID userId);

    Optional<RefreshToken> findByUser_Email(String email);
}
