package com.bankapp.userservice.service;

import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.RefreshToken;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;

public interface RefreshTokenService {

    RefreshToken createInitial(User user);

    RefreshToken refresh(RefreshTokenRequest request, RefreshTokenResponse response);
}
