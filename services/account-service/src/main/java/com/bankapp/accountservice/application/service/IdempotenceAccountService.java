package com.bankapp.accountservice.application.service;


import com.bankapp.accountservice.application.port.in.CreditAccountCommand;
import com.bankapp.accountservice.application.port.in.DebitAccountCommand;
import com.bankapp.accountservice.application.port.in.UpdateAccountBalanceUseCase;
import com.bankapp.accountservice.application.port.out.AccountRepository;
import com.bankapp.accountservice.application.port.out.EventPublisher;
import com.bankapp.accountservice.application.port.out.InboxRepository;
import com.bankapp.accountservice.domain.exception.DomainValidationException;
import com.bankapp.accountservice.domain.model.*;
import com.bankapp.accountservice.domain.model.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotenceAccountService implements UpdateAccountBalanceUseCase {

    private final InboxRepository inboxRepository;

    @Qualifier("defaultAccountService")
    private final UpdateAccountBalanceUseCase decorated;

    @Override
    @Transactional
    public void processDebitFunds(DebitAccountCommand debitAccountCommand) {
        Inbox inbox = Inbox.createInboxEvent(
                debitAccountCommand.commandId(),
                debitAccountCommand.timestamp());
        if (!inboxRepository.insertIfNotExists(inbox)) {
            log.info("Debit command {} already processed", debitAccountCommand);
            return;
        }

        decorated.processDebitFunds(debitAccountCommand);
    }


    @Override
    @Transactional
    public void processCreditFunds(CreditAccountCommand creditAccountCommand) {
        Inbox inbox = Inbox.createInboxEvent(
                creditAccountCommand.commandId(),
                creditAccountCommand.timestamp()
        );
        if(!inboxRepository.insertIfNotExists(inbox)) {
            log.info("Credit command {} already processed", creditAccountCommand);
            return;
        }
        decorated.processCreditFunds(creditAccountCommand);
    }
}
