package com.bankapp.userservice.controller;

import com.bankapp.common.client.userserviceauth.api.AuthApiV1;
import com.bankapp.common.client.userserviceauth.model.*;
import com.bankapp.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequiredArgsConstructor
public class AuthControllerV1 implements AuthApiV1 {

    private final AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {

        RegisterResponse response = authService.register(registerRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getUserId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<LogoutResponse> logout(LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RefreshAccessTokenResponse> refresh(RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refresh(refreshTokenRequest));
    }
}
