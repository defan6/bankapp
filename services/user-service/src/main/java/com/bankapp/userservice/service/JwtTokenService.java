package com.bankapp.userservice.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JwtTokenService {

    String generateToken(String username, Set<String> role);

    String extractUsername(String token);

    List<String> extractRole(String token);

    boolean validateToken(String token);

    UsernamePasswordAuthenticationToken authenticate(String token);

    Date extractExpiration(String token);
}
