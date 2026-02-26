package com.bankapp.userservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RegisterValidatorHandler extends RuntimeException {
    private final List<String> errors;
}
