package com.bankapp.userservice.service.impl;

import com.bankapp.userservice.domain.CustomUserDetail;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultJwtTokenService implements JwtTokenService {

    private final JwtUtil jwtUtil;

    private final CustomUserDetailService userDetailService;

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

    @Override
    public UsernamePasswordAuthenticationToken authenticate(String token) {
        String username = extractUsername(token);

        UserDetails userDetail = userDetailService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken (
                userDetail, null, userDetail.getAuthorities()
        );
    }

    public Date extractExpiration(String token) {
        return jwtUtil.extractExpiration(token);
    }
}
