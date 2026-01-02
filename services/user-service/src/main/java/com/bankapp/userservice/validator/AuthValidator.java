package com.bankapp.userservice.validator;

import com.bankapp.common.client.userserviceauth.model.LoginRequest;
import com.bankapp.common.client.userserviceauth.model.RegisterRequest;
import com.bankapp.userservice.exception.AuthenticationValidationException;
import com.bankapp.userservice.exception.ExistsValidatorHandler;
import com.bankapp.userservice.exception.LoginValidatorException;
import com.bankapp.userservice.exception.RegisterValidatorHandler;
import com.bankapp.userservice.repository.RefreshTokenRepository;
import com.bankapp.userservice.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final Validator validator;

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public void registerValidator(RegisterRequest request) {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        List<String> errors = new ArrayList<>(violations
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList()
        );

        if (!errors.isEmpty()) {
            throw new RegisterValidatorHandler(errors);
        }
        if (userRepository.existsUserByEmail(request.getEmail())) {
            throw new ExistsValidatorHandler("User with " + request.getEmail() + " email already exists");
        }
    }

    public void loginValidator(LoginRequest request) {
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        List<String> errors = new ArrayList<>(violations
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList()
        );

        if (!errors.isEmpty()) {
            throw new LoginValidatorException(errors);
        }
        if (!userRepository.existsUserByEmail(request.getEmail())) {
            throw new ExistsValidatorHandler("User with " + request.getEmail() + " email not exists");
        }
        if (refreshTokenRepository.findByUser_Email(request.getEmail()).isPresent()) {
            throw new AuthenticationValidationException("User already authenticated");
        }
    }
}
