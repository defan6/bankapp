package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final AuthService authService;

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        return authService.register(request);
    }

    @Override
    public LoginResponse loginUser(LoginRequest login) {
        return authService.authenticate(login);
    }

    @Override
    public UserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        return authService.refresh(request);
    }
}
