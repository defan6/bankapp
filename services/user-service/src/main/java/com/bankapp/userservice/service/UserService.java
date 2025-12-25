package com.bankapp.userservice.service;

import com.bankapp.common.client.userservice.model.*;

public interface UserService {

    RegisterResponse registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest login);

    UserResponse getCurrentUser();
}
