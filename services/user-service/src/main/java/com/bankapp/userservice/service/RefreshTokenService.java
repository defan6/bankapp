package com.bankapp.userservice.service;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.RefreshToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface RefreshTokenService {

    RefreshToken getRefreshToken(UserDetails userDetails);

    boolean isRefreshToken(String token);
}
