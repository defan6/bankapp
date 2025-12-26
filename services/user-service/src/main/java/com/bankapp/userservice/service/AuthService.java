package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.LoginRequest;
import com.bankapp.common.client.userservice.model.LoginResponse;
import com.bankapp.common.client.userservice.model.RegisterRequest;
import com.bankapp.common.client.userservice.model.RegisterResponse;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {

    LoginResponse authenticate(LoginRequest login);

    RegisterResponse register(RegisterRequest request);

    Optional<Authentication> authenticateToken(String token);
}
