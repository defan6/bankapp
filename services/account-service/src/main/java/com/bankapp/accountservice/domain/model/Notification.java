package com.bankapp.accountservice.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Notification {
    private final List<Error> errors = new ArrayList<>();


    public void addError(String code, String message) {
        errors.add(new Error(code, message));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }


    public List<Error> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
