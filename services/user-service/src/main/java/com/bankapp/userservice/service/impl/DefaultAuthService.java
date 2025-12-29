package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.*;
import com.bankapp.userservice.domain.CustomUserDetails;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.token.dto.AccessTokenResponse;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import com.bankapp.userservice.mapper.UserMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.service.RefreshTokenService;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        String username = userDetails.getUsername();
        UUID userId = userDetails.getUserId();
        AccessTokenResponse accessToken = jwtTokenService.generateAccessToken(userId, username, roles);

        RefreshTokenResponse refreshToken = refreshTokenService.getRefreshToken(userDetails);

        return generateLoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RegisterResponse getRegister(RegisterRequest request) {
        User user = userMapper.toUser(request);
        user.getRoles().add("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toRegisterResponse(userRepository.save(user));
    }

    @Override
    public Optional<Authentication> getAuthentication(String token) {
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

        CustomUserDetails userDetail = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetail.getUser();

        return userMapper.toUserResponse(user);
    }


    private LoginResponse generateLoginResponse(AccessTokenResponse accessToken, RefreshTokenResponse refreshToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken.accessToken());
        loginResponse.setRefreshToken(refreshToken.token());
        return loginResponse;
    }
}
