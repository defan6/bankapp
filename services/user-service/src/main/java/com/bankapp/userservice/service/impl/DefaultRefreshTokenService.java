package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.CustomUserDetails;
import com.bankapp.userservice.domain.RefreshToken;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.dto.RefreshTokenDetails;
import com.bankapp.userservice.mapper.RefreshTokenMapper;
import com.bankapp.userservice.repository.RefreshTokenRepository;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DefaultRefreshTokenService implements RefreshTokenService {

    private final JwtTokenService jwtTokenService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public RefreshToken getRefreshToken(UserDetails userDetails) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        String username = customUserDetails.getUsername();
        User user = customUserDetails.getUser();
        RefreshTokenDetails refreshTokenDetails  = jwtTokenService.generateRefreshToken(username);
        Instant expirationAt = Instant.ofEpochMilli(refreshTokenDetails.expirationAt());
        RefreshToken refreshToken = new RefreshToken(user, refreshTokenDetails.token(), expirationAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token) != null;
    }


}
