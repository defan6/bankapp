package com.bankapp.userservice.domain.token.dto;

public record RefreshTokenDetails(String token, long expirationAt) {
}
