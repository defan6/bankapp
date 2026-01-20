package com.bankapp.accountservice.application.service;

import com.bankapp.accountservice.application.port.in.*;
import com.bankapp.accountservice.application.port.out.AccountRepository;
import com.bankapp.accountservice.application.port.out.EventPublisher;
import com.bankapp.accountservice.domain.exception.AccountNotFoundException;
import com.bankapp.accountservice.domain.exception.DomainValidationException;
import com.bankapp.accountservice.domain.model.*;
import com.bankapp.accountservice.domain.model.Error;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAccountService implements CreateAccountUseCase, GetAccountQuery, UpdateAccountBalanceUseCase {
    private final AccountRepository accountRepository;

    private final EventPublisher eventPublisher;

    @Override
    public Account createAccount(CreateAccountCommand createAccountCommand) {
        Notification notification = new Notification();
        Account account = Account.createAccount(createAccountCommand.userId(),
                createAccountCommand.currency(), notification);
        if (notification.hasErrors()) {
            throw new DomainValidationException(notification.getErrors(),
                    null, // accountId еще не создан
                    null, // correlationId отсутствует в этом use case
                    null, // amount отсутствует
                    null, // balance отсутствует
                    null // headers отсутствуют
            );
        }
        return accountRepository.save(account);

    }

    @Override
    public Account getAccount(UUID accountId) {
        return accountRepository.findAccount(accountId)
                .orElseThrow(
                        () -> new AccountNotFoundException("Account not found with id: " + accountId)
                );
    }

    @Override
    public List<Account> getUserAccounts(UUID userId) {
        return accountRepository.findAllAccountsByUserId(userId);
    }

    @Override
    @Transactional
    public void processDebitFunds(DebitAccountCommand debitAccountCommand) {
        Money amountToDebit = Money.of(debitAccountCommand.amount(), debitAccountCommand.currency());

        Account account = accountRepository.findAccount(debitAccountCommand.accountId())
                .orElse(null);

        if (account == null) {
            throw new DomainValidationException(

                    List.of(new Error("ACCOUNT_NOT_FOUND", "Account not found with id: " + debitAccountCommand.accountId())),
                    debitAccountCommand.accountId(),
                    debitAccountCommand.correlationId(),
                    amountToDebit,
                    null,
                    debitAccountCommand.headers()

            );
        }

        if (!account.getCurrency().name().equals(debitAccountCommand.currency().name())) {
            throw new DomainValidationException(
                    List.of(new Error("CURRENCY_MISMATCH", "Debit currency does not match account currency")),
                    debitAccountCommand.accountId(),
                    debitAccountCommand.correlationId(),
                    amountToDebit,
                    account.getBalance(),
                    debitAccountCommand.headers()
            );
        }

        Notification notification = new Notification();
        account.debit(amountToDebit, notification);

        if (notification.hasErrors()) {
            throw new DomainValidationException(
                    notification.getErrors(),
                    debitAccountCommand.accountId(),
                    debitAccountCommand.correlationId(),
                    amountToDebit,
                    account.getBalance(),
                    debitAccountCommand.headers()
            );
        }
        accountRepository.save(account);
        AccountDebitedEvent successEvent = generateAccountDebitedEvent(debitAccountCommand, account);
        eventPublisher.publish(successEvent);

    }

    private AccountDebitedEvent generateAccountDebitedEvent(DebitAccountCommand debitAccountCommand, Account account) {
        return AccountDebitedEvent.createAccountDebitedEvent(
                debitAccountCommand.accountId(),
                debitAccountCommand.correlationId(),
                Money.of(debitAccountCommand.amount(), debitAccountCommand.currency()),
                account.getBalance(),
                debitAccountCommand.headers()
        );
    }

    @Override
    public void processCreditFunds(CreditAccountCommand creditAccountCommand) {
        Money amountToCredit = Money.of(creditAccountCommand.amount(), creditAccountCommand.currency());
        Account account = accountRepository.findAccount(creditAccountCommand.accountId())
                .orElse(null);
        if (account == null) {
            throw new DomainValidationException(
                    List.of(new Error("ACCOUNT_NOT_FOUND", "Account not found with id: " + creditAccountCommand.accountId())),
                    creditAccountCommand.accountId(),
                    creditAccountCommand.correlationId(),
                    amountToCredit,
                    null,
                    creditAccountCommand.headers()
            );
        }


        if (!account.getCurrency().name().equals(creditAccountCommand.currency().name())) {
            throw new DomainValidationException(
                    List.of(new Error("CURRENCY_MISMATCH", "Debit currency does not match account currency")),
                    creditAccountCommand.accountId(),
                    creditAccountCommand.correlationId(),
                    amountToCredit,
                    account.getBalance(),
                    creditAccountCommand.headers()
            );
        }

        Notification notification = new Notification();
        account.credit(amountToCredit, notification);

        if (notification.hasErrors()) {
            throw new DomainValidationException(
                    notification.getErrors(),
                    creditAccountCommand.accountId(),
                    creditAccountCommand.correlationId(),
                    amountToCredit,
                    account.getBalance(),
                    creditAccountCommand.headers()
            );
        }
        accountRepository.save(account);
        AccountCreditedEvent successEvent = generateAccountCreditedEvent(creditAccountCommand, account);
        eventPublisher.publish(successEvent);
    }


    private AccountCreditedEvent generateAccountCreditedEvent(CreditAccountCommand creditAccountCommand, Account account) {
        return AccountCreditedEvent.createAccountCreditedEvent(
                creditAccountCommand.accountId(),
                creditAccountCommand.correlationId(),
                Money.of(creditAccountCommand.amount(), creditAccountCommand.currency()),
                account.getBalance(),
                creditAccountCommand.headers()
        );
    }
}
