package com.bankapp.accountservice.application.port.in;

import java.util.UUID;

public record BatchCreateAccountResult(
        UUID userId,
        boolean success,
        UUID accountId,
        String error
) {
}
