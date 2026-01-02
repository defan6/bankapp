package com.bankapp.userservice.exception.handler;

import com.bankapp.userservice.domain.detail.CustomProblemDetail;
import com.bankapp.userservice.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionValidatorHandler {

    @ExceptionHandler(ExistsValidatorHandler.class)
    public CustomProblemDetail handleExistsValidatorException(ExistsValidatorHandler e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.BAD_REQUEST);
        pb.setTimestamp(Instant.now());
        pb.setDetail(e.getMessage());
        return pb;
    }

    @ExceptionHandler(LoginValidatorException.class)
    public CustomProblemDetail handleLoginValidatorException(LoginValidatorException e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.BAD_REQUEST);
        pb.setProperty("errors", e.getErrors());
        return pb;
    }

    @ExceptionHandler(RegisterValidatorHandler.class)
    public CustomProblemDetail handleRegisterValidatorHandler(RegisterValidatorHandler e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.BAD_REQUEST);
        pb.setProperty("errors", e.getErrors());
        return pb;
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public CustomProblemDetail handleTokenNotFoundException(TokenNotFoundException e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.NOT_FOUND);
        pb.setTimestamp(Instant.now());
        pb.setDetail(e.getMessage());
        return pb;
    }

    @ExceptionHandler(UserIdNotFoundException.class)
    public CustomProblemDetail handleUserIdNotFoundException(UserIdNotFoundException e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.NOT_FOUND);
        pb.setTimestamp(Instant.now());
        pb.setDetail(e.getMessage());
        return pb;
    }

    @ExceptionHandler(AuthenticationValidationException.class)
    public CustomProblemDetail handleAuthenticationValidationException(AuthenticationValidationException e) {
        CustomProblemDetail pb = new CustomProblemDetail();
        pb.setStatus(HttpStatus.BAD_REQUEST);
        pb.setTimestamp(Instant.now());
        pb.setDetail(e.getMessage());
        return pb;
    }
}