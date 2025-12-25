package com.bankapp.userservice.service;

import java.util.List;
import java.util.Set;

public interface JwtTokenService {

    String generateToken(String username, Set<String> role);

    String extractUsername(String token);

    List<String> extractRole(String token);

    boolean validateToken(String token);
}
