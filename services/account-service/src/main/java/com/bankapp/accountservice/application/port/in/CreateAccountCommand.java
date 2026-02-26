package com.bankapp.accountservice.application.port.in;

import com.bankapp.accountservice.domain.model.Currency;

import java.util.UUID;

public record CreateAccountCommand(UUID userId, Currency currency) {
}
