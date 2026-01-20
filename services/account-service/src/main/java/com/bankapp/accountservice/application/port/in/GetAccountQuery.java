package com.bankapp.accountservice.application.port.in;

import com.bankapp.accountservice.domain.model.Account;

import java.util.List;
import java.util.UUID;

public interface GetAccountQuery {

    Account getAccount(UUID accountId);

    List<Account> getUserAccounts(UUID userId);
}
