package com.bankapp.accountservice.adapters.in.messaging;


import com.bankapp.accountservice.adapters.in.messaging.mapper.MyAccountCommandMapper;
import com.bankapp.accountservice.application.port.in.CreditAccountCommand;
import com.bankapp.accountservice.application.port.in.DebitAccountCommand;
import com.bankapp.accountservice.application.port.in.UpdateAccountBalanceUseCase;
import com.bankapp.accountservice.application.port.out.EventPublisher;
import com.bankapp.accountservice.domain.exception.DomainValidationException;
import com.bankapp.accountservice.domain.model.AccountCreditFailedEvent;
import com.bankapp.accountservice.domain.model.AccountDebitFailedEvent;
import com.bankapp.accountservice.domain.model.Error;
import com.bankapp.events.account.v1.CreditFunds;
import com.bankapp.events.account.v1.DebitFunds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountCommandConsumer {

    private final MyAccountCommandMapper accountCommandMapper;

    private final UpdateAccountBalanceUseCase updateAccountBalanceUseCase;

    private final EventPublisher eventPublisher;

    @KafkaListener(topics = {"account-debit-topic"}, containerFactory = "kafkaListenerContainerFactory")
    public void handleDebitFundsCommand(@Payload DebitFunds debitFunds, @Headers MessageHeaders headers) {
        log.info("Received DebitFunds command from Kafka: {}, headers: {}", debitFunds, headers);
        Map<String, Object> convertedHeaders = convertKafkaHeaders(headers);
        DebitAccountCommand debitAccountCommand = accountCommandMapper.toDebitAccountCommand(debitFunds, convertedHeaders);
        try {
            updateAccountBalanceUseCase.processDebitFunds(debitAccountCommand);
        } catch (DomainValidationException e) {
            log.warn("Business validation failed for debit command {}: {}", debitAccountCommand, e.getMessage());
            AccountDebitFailedEvent failureEvent = generateAccountDebitFailedEvent(e);
            eventPublisher.publish(failureEvent);
        }
    }

    private AccountDebitFailedEvent generateAccountDebitFailedEvent(DomainValidationException e) {
        String reason = e.getErrors().stream().map(Error::code).collect(Collectors.joining(", "));
        return AccountDebitFailedEvent.createAccountDebitFailedEvent(
                e.getAccountId(),
                e.getCorrelationId(),
                e.getTryToDebitAmount(),
                e.getBalance(),
                reason,
                e.getHeaders()
        );
    }


    @KafkaListener(topics = {"account-credit-topic"}, containerFactory = "kafkaListenerContainerFactory")
    public void handleCreditFundsCommand(@Payload CreditFunds creditFunds, @Headers MessageHeaders headers) {
        log.info("Received CreditFunds command from Kafka: {}, headers: {}", creditFunds, headers);
        Map<String, Object> convertedHeaders = convertKafkaHeaders(headers);
        CreditAccountCommand creditAccountCommand = accountCommandMapper.toCreditAccountCommand(creditFunds, convertedHeaders);
        try {
            updateAccountBalanceUseCase.processCreditFunds(creditAccountCommand);
        } catch (DomainValidationException e) {
            log.warn("Business validation failed for credit command {}: {}", creditFunds, e.getMessage());
            AccountCreditFailedEvent failureEvent = generateAccountCreditFailedEvent(e);
            eventPublisher.publish(failureEvent);
        }
    }

    private AccountCreditFailedEvent generateAccountCreditFailedEvent(DomainValidationException e) {
        String reason = e.getErrors().stream().map(Error::code).collect(Collectors.joining(", "));
        return AccountCreditFailedEvent.createAccountCreditFailedEvent(
                e.getAccountId(),
                e.getCorrelationId(),
                e.getTryToDebitAmount(),
                e.getBalance(),
                reason,
                e.getHeaders()
        );
    }


    @KafkaHandler(isDefault = true)
    public void handleUnknownCommand(Object command) {
        log.info("Received unknown command type: {}", command);
    }


    private Map<String, Object> convertKafkaHeaders(MessageHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        headers.forEach((k, v) -> {
            String stringValue;
            if (v instanceof byte[]) {
                stringValue = new String((byte[]) v, StandardCharsets.UTF_8);
            } else {
                stringValue = v != null ? v.toString() : null;
            }
            if (stringValue != null) {
                map.put(k, stringValue);
            }
        });
        return map;
    }
}

