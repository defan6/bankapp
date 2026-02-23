package com.bankapp.accountservice.application.port.in;

import com.bankapp.accountservice.domain.model.Account;

import java.util.List;

public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand createAccountRequest);

    List<BatchCreateAccountResult> createAccountsBatch(List<CreateAccountCommand> commands);
}
