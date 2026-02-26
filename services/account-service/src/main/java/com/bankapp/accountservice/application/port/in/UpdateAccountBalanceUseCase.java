package com.bankapp.accountservice.application.port.in;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface UpdateAccountBalanceUseCase {
    void processDebitFunds(DebitAccountCommand debitAccountCommand);

    void processCreditFunds(CreditAccountCommand creditAccountCommand);
}
