package com.bankapp.userservice.service.impl;

import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefaultJwtTokenService implements JwtTokenService {

    private final JwtUtil jwtUtil;

    @Override
    public String generateToken(String username, Set<String> role) {
        return jwtUtil.generateToken(username, role);
    }

    @Override
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public List<String> extractRole(String token) {
        return jwtUtil.extractRole(token);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }
}
