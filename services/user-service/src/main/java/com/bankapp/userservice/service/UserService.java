package com.bankapp.userservice.service;

import com.bankapp.common.client.userserviceuser.model.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(String email);
}
