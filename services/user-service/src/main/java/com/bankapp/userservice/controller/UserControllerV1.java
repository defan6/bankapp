package com.bankapp.userservice.controller;

import com.bankapp.common.client.userservice.api.UserApiV1;
import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserControllerV1 implements UserApiV1 {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        return null;
    }

    @Override
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @Override
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest registerRequest) {

        RegisterResponse response = userService.registerUser(registerRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getUserId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
