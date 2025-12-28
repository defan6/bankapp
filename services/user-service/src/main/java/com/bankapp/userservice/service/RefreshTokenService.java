package com.bankapp.userservice.service;

import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.RefreshToken;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;

public interface RefreshTokenService {

    RefreshToken createInitial(User user);

    User refresh(RefreshTokenRequest request, RefreshTokenResponse response);
}
