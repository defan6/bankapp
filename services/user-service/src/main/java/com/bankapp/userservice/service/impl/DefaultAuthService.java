package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.CustomUserDetail;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.RefreshToken;
import com.bankapp.userservice.domain.token.dto.RefreshTokenRequest;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import com.bankapp.userservice.mapper.UserMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.service.RefreshTokenService;
import com.bankapp.userservice.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAuthService implements AuthService {

    private final JwtTokenService jwtTokenService;

    private final AuthenticationManager authenticationManager;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final RefreshTokenService refreshTokenService;

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest login) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                );

        log.info("Authentication start");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();

        Set<String> role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenService.generateToken(userDetail.getUsername(), role);

        RefreshToken refreshToken = refreshTokenService.createInitial(userDetail.getUser());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(jwtTokenService.generateToken(userDetail.getUsername(), role));
        loginResponse.setRefreshToken(refreshToken.getToken());

        return loginResponse;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User user = userMapper.toUser(request);
        user.getRoles().add("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toRegisterResponse(userRepository.save(user));
    }

    @Override
    public Optional<Authentication> authenticateToken(String token) {
        try {
            Authentication authentication = jwtTokenService.authenticate(token);

            return Optional.of(authentication);
        }
        catch (AuthenticationException e) {
            return Optional.empty();
        }
    }

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();

        User user = userDetail.getUser();

        return userMapper.toUserResponse(user);
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        RefreshTokenResponse response = new RefreshTokenResponse();
        RefreshToken token = refreshTokenService.refresh(request, response);
        response.setAccessToken(jwtTokenService.generateToken(token.getUser().getEmail(), token.getUser().getRoles()));
        return response;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        tokenBlacklistService.blacklist(
                accessToken, refreshToken, ((jwtTokenService.extractExpiration(accessToken)).getTime() - System.currentTimeMillis()) / 1000
        );
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return tokenBlacklistService.isBlacklisted(accessToken);
    }


}
