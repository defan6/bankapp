package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.adapters.out.jpa.mapper.OutboxEventPublisherMapper;
import com.bankapp.accountservice.application.port.out.EventPublisher;
import com.bankapp.accountservice.application.port.out.OutboxRepository;
import com.bankapp.accountservice.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisherAdapter implements EventPublisher {

    private final OutboxRepository outboxRepository;

    private final OutboxEventPublisherMapper outboxEventPublisherMapper;

    @Override
    public void publish(AccountDebitedEvent accountDebitedEvent) {
        Outbox outbox = outboxEventPublisherMapper.toOutbox(accountDebitedEvent);
        outboxRepository.save(outbox);
    }


    @Override
    public void publish(AccountDebitFailedEvent accountDebitFailedEvent) {
        Outbox outbox = outboxEventPublisherMapper.toOutbox(accountDebitFailedEvent);
        outboxRepository.save(outbox);
    }


    @Override
    public void publish(AccountCreditedEvent accountCreditedEvent) {
        Outbox outbox = outboxEventPublisherMapper.toOutbox(accountCreditedEvent);
        outboxRepository.save(outbox);
    }

    @Override
    public void publish(AccountCreditFailedEvent accountCreditFailedEvent) {
        Outbox outbox = outboxEventPublisherMapper.toOutbox(accountCreditFailedEvent);
        outboxRepository.save(outbox);
    }
}
