package com.bankapp.userservice.controller;

import com.bankapp.common.client.userservice.api.ApiApi;
import com.bankapp.common.client.userservice.model.*;
import org.springframework.http.ResponseEntity;

public class UserController implements ApiApi {
    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        return null;
    }

    @Override
    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<RegisterResponse> registerUser(RegisterRequest registerRequest) {
        return null;
    }
}
