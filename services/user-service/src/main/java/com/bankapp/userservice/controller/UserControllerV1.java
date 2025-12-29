package com.bankapp.userservice.controller;

import com.bankapp.common.client.userserviceuser.api.UserApiV1;
import com.bankapp.common.client.userserviceuser.model.UserResponse;
import com.bankapp.userservice.domain.CustomUserDetails;
import com.bankapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserControllerV1 implements UserApiV1 {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getUsername();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }
}
