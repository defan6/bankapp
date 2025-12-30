package com.bankapp.userservice.service;

public interface TokenBlacklistService {

    void blacklist(String token, String refreshToken, Long ttlSeconds);

    boolean isBlacklisted(String accessToken);
}
