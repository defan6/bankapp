package com.bankapp.userservice.service;

import com.bankapp.common.client.userserviceauth.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {

    LoginResponse login(LoginRequest login);

    RegisterResponse register(RegisterRequest request);

    Optional<Authentication> getAuthentication(String token);

    RefreshAccessTokenResponse refresh(RefreshTokenRequest refreshTokenRequest);

    LogoutResponse logout(LogoutRequest logoutRequest);

    boolean isBlacklisted(String accessToken);

    boolean isRefreshToken(String token);
}
