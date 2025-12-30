package com.bankapp.userservice.repository;

import com.bankapp.userservice.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    RefreshToken findByToken(String token);

    RefreshToken findByUser_Id(UUID userId);
}
