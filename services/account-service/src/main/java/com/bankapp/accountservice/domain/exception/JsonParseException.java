package com.bankapp.accountservice.domain.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonParseException extends JsonProcessingException {
    public JsonParseException(String message) {
        super(message);
    }
}
