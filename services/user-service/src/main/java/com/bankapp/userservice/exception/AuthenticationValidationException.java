package com.bankapp.userservice.exception;

public class AuthenticationValidationException extends RuntimeException {
    public AuthenticationValidationException(String message) {
        super(message);
    }
}
