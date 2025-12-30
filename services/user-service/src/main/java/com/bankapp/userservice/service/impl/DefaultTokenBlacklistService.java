package com.bankapp.userservice.service.impl;

import com.bankapp.userservice.repository.RefreshTokenRepository;
import com.bankapp.userservice.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DefaultTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void blacklist(String accessToken, String refreshToken, Long ttlSeconds) {
        stringRedisTemplate.opsForValue()
                .set(accessToken, "revoked", ttlSeconds, TimeUnit.SECONDS);

        refreshTokenRepository.delete(refreshTokenRepository.findByToken(refreshToken));
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return stringRedisTemplate.hasKey(accessToken);
    }
}
