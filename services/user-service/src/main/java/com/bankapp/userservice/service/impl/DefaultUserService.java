package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.mapper.UserMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        return null;
    }
}
