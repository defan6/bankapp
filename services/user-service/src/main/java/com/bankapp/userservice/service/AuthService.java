package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {

    LoginResponse authenticate(LoginRequest login);

    RegisterResponse register(RegisterRequest request);

    Optional<Authentication> authenticateToken(String token);

    UserResponse getCurrentUser();

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken, String refreshToken);

    boolean isBlacklisted(String accessToken);
}
