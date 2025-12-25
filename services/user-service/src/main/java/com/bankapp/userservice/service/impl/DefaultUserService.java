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

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        User user = userMapper.toUser(request);
        user.getRoles().add("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toRegisterResponse(userRepository.save(user));
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
