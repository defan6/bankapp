package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;

public interface UserService {

    RegisterResponse registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest login);

    UserResponse getCurrentUser();

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
