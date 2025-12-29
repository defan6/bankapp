package com.bankapp.userservice.domain.token.dto;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenResponse(UUID userId, String token, Instant expirationAt) {
}
