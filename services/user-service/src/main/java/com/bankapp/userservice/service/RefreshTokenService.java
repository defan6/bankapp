package com.bankapp.userservice.service;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.RefreshToken;
import com.bankapp.userservice.domain.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface RefreshTokenService {

    RefreshTokenResponse getRefreshToken(User user);

    boolean isRefreshToken(String token);
}
