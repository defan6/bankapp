package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userserviceauth.model.*;
import com.bankapp.userservice.domain.CustomUserDetails;
import com.bankapp.userservice.domain.token.dto.AccessTokenResponse;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import com.bankapp.userservice.mapper.AuthMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import com.bankapp.userservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
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

    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest login) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                );

        log.info("Authentication start");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        String username = userDetails.getUsername();
        UUID userId = userDetails.getUserId();
        AccessTokenResponse accessToken = jwtTokenService.generateAccessToken(userId, username, authorities);

        RefreshTokenResponse refreshToken = refreshTokenService.getRefreshToken(userDetails);

        return generateLoginResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
//        User user = authMapper.toUser(request);
//        user.getRoles().add("ROLE_USER");
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        return authMapper.toRegisterResponse(userRepository.save(user));
        return null;
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



    private LoginResponse generateLoginResponse(AccessTokenResponse accessToken, RefreshTokenResponse refreshToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken.accessToken());
        loginResponse.setRefreshToken(refreshToken.token());
        return loginResponse;
    }


    @Override
    public ResponseEntity<com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse> refresh(RefreshTokenRequest refreshTokenRequest) {
        return null;
    }
}
