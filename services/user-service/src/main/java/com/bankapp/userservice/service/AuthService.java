package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.LoginRequest;
import com.bankapp.common.client.userservice.model.LoginResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    LoginResponse authenticate(LoginRequest login);
}
