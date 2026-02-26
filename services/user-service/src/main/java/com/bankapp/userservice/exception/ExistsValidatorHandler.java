package com.bankapp.userservice.exception;

public class ExistsValidatorHandler extends RuntimeException {
    public ExistsValidatorHandler(String message) {
        super(message);
    }
}
