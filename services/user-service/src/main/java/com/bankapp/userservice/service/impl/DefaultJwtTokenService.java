package com.bankapp.userservice.service.impl;

import com.bankapp.userservice.domain.token.dto.AccessTokenResponse;
import com.bankapp.userservice.domain.token.dto.RefreshTokenDetails;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultJwtTokenService implements JwtTokenService {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailService userDetailService;

    @Override
    public AccessTokenResponse generateAccessToken(UUID userId, String username, Set<String> roles) {
        String token = jwtUtil.generateAccessToken(userId, username, roles);
        return new AccessTokenResponse(token);
    }

    @Override
    public RefreshTokenDetails generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }

    @Override
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public Set<String> extractRole(String token) {
        return jwtUtil.extractRoles(token);
    }

    @Override
    public UUID extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    @Override
    public UsernamePasswordAuthenticationToken authenticate(String token) {
        String username = extractUsername(token);

        UserDetails userDetail = userDetailService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken (
                userDetail, null, userDetail.getAuthorities()
        );
    }

    @Override
    public Date extractExpiration(String token) {
        return jwtUtil.extractExpiration(token);
    }
}
