package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.*;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface AuthService {

    LoginResponse authenticate(LoginRequest login);

    RegisterResponse getRegister(RegisterRequest request);

    Optional<Authentication> getAuthentication(String token);

    UserResponse getCurrentUser();

}
