package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.adapters.out.jpa.mapper.AccountJpaMapper;
import com.bankapp.accountservice.application.port.out.AccountRepository;
import com.bankapp.accountservice.domain.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountJpaAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    private final AccountJpaMapper accountJpaMapper;

    @Override
    public Account save(Account account) {
        AccountJpaEntity accountJpaEntity = accountJpaMapper.toAccountJpaEntity(account);
        return accountJpaMapper.toDomainAccount(accountJpaRepository.save(accountJpaEntity));
    }

    @Override
    public Optional<Account> findAccount(UUID id) {
        return accountJpaRepository.findById(id)
                .map(accountJpaMapper::toDomainAccount);
    }

    @Override
    public List<Account> findAllAccountsByUserId(UUID userId) {
        return accountJpaRepository.findAllByUserId(userId)
                .stream()
                .map(accountJpaMapper::toDomainAccount)
                .toList();
    }
}
