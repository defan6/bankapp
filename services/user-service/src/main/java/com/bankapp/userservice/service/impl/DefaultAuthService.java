package com.bankapp.userservice.service.impl;

import com.bankapp.common.client.userservice.model.LoginRequest;
import com.bankapp.common.client.userservice.model.LoginResponse;
import com.bankapp.common.client.userservice.model.RegisterRequest;
import com.bankapp.common.client.userservice.model.RegisterResponse;
import com.bankapp.userservice.domain.CustomUserDetail;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.mapper.UserMapper;
import com.bankapp.userservice.repository.UserRepository;
import com.bankapp.userservice.service.AuthService;
import com.bankapp.userservice.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Override
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

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.token(jwtTokenService.generateToken(userDetail.getUsername(), role));

        return loginResponse;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        User user = userMapper.toUser(request);
        user.getRoles().add("ROLE_USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toRegisterResponse(userRepository.save(user));
    }

    @Override
    public Optional<Authentication> authenticateToken(String token) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(jwtTokenService.authenticate(token));

            return Optional.of(authentication);
        }
        catch (AuthenticationException e) {
            return Optional.empty();
        }
    }
}
