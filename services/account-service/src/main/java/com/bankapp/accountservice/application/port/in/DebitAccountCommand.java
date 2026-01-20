package com.bankapp.accountservice.application.port.in;

import com.bankapp.accountservice.domain.model.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record DebitAccountCommand(
        @NotNull UUID commandId,
        @NotNull UUID correlationId,
        @NotNull UUID accountId,
        @NotNull @Positive BigDecimal amount,
        @NotNull Map<String, Object> headers,
        @NotNull Currency currency,
        @NotNull LocalDateTime timestamp
) {
}
