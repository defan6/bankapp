package com.bankapp.accountservice.application.port.out;

import com.bankapp.accountservice.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findAccount(UUID id);

    List<Account> findAllAccountsByUserId(UUID userId);
}
