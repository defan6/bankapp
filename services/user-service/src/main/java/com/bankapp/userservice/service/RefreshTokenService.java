package com.bankapp.userservice.service;

import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface RefreshTokenService {

    RefreshTokenResponse getRefreshToken(UserDetails userDetails);

    boolean isRefreshToken(String token);
}
