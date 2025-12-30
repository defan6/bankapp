package com.bankapp.userservice.service;

import com.bankapp.userservice.domain.token.dto.AccessTokenResponse;
import com.bankapp.userservice.domain.token.dto.RefreshTokenDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public interface JwtTokenService {

    AccessTokenResponse generateAccessToken(UUID userId, String username, Set<String> roles);

    RefreshTokenDetails generateRefreshToken(String username);

    String extractUsername(String token);

    Set<String> extractRole(String token);

    UUID extractUserId(String token);

    UsernamePasswordAuthenticationToken authenticate(String token);

    Date extractExpiration(String token);

    boolean validateToken(String token);
}
