package com.bankapp.userservice.service;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.User;

public interface RefreshTokenService {

    RefreshTokenResponse createRefreshToken(User user);

    RefreshTokenResponse updateRefreshToken(User user);

    boolean isRefreshToken(String token);
}
