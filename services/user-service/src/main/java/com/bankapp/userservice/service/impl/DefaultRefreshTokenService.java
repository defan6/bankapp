package com.bankapp.userservice.service.impl;

import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.RefreshToken;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import com.bankapp.userservice.repository.RefreshTokenRepository;
import com.bankapp.userservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultRefreshTokenService implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenReposotory;

    private static final Duration SESSION_TTL = Duration.ofDays(30);

    @Override
    public RefreshToken createInitial(User user) {
        Instant now = Instant.now();
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setSessionExpiresAt(now.plus(SESSION_TTL));
        refreshTokenReposotory.save(token);
        return token;
    }

    @Override
    public RefreshToken refresh(RefreshTokenRequest request, RefreshTokenResponse response) {
        RefreshToken token = refreshTokenReposotory.findByToken(request.getToken());
        token.setToken(UUID.randomUUID().toString());
        response.setRefreshToken(token.getToken());
        return refreshTokenReposotory.save(token);
    }

}
