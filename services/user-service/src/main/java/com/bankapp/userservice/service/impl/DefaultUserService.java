package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userserviceuser.model.UserResponse;
import com.bankapp.userservice.mapper.UserMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .map(userMapper::toUserResponse)
                .orElseThrow();
    }

}
