package com.bankapp.accountservice.domain.exception;

import com.bankapp.accountservice.domain.marker.DomainEvent;
import com.bankapp.accountservice.domain.model.AccountDebitFailedEvent;
import com.bankapp.accountservice.domain.model.Error;
import com.bankapp.accountservice.domain.model.Money;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class DomainValidationException extends RuntimeException {
    private final List<Error> errors;
    private final UUID accountId;
    private final UUID correlationId;
    private final Money tryToDebitAmount;
    private final Money balance;
    private final Map<String, Object> headers;

    public DomainValidationException(List<Error> errors, UUID accountId, UUID correlationId, Money tryToDebitAmount, Money balance, Map<String, Object> headers) {
        super("Domain validation failed" + errors.stream().map(Error::code).collect(Collectors.joining(", ")));
        this.errors = errors;
        this.accountId = accountId;
        this.correlationId = correlationId;
        this.tryToDebitAmount = tryToDebitAmount;
        this.balance = balance;
        this.headers = headers;
    }
}
