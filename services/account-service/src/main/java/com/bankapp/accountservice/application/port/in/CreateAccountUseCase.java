package com.bankapp.accountservice.application.port.in;

import com.bankapp.accountservice.domain.model.Account;

public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand createAccountRequest);

}
