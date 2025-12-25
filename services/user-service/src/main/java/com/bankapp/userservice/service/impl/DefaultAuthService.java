package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.LoginRequest;
import com.bankapp.common.client.userservice.model.LoginResponse;
import com.bankapp.userservice.domain.CustomUserDetail;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final JwtTokenService jwtTokenService;

    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse authenticate(LoginRequest login) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();

        Set<String> role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.token(jwtTokenService.generateToken(userDetail.getUsername(), role));

        return loginResponse;
    }
}
