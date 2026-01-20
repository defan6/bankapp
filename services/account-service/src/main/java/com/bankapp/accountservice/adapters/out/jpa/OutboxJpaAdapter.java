package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.adapters.out.jpa.mapper.OutboxJpaMapper;
import com.bankapp.accountservice.application.port.out.OutboxRepository;
import com.bankapp.accountservice.domain.model.Outbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxJpaAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;

    private final OutboxJpaMapper outboxJpaMapper;

    @Override
    public void save(Outbox outbox) {
        OutboxJpaEntity outboxJpaEntity = outboxJpaMapper.toOutboxJpaEntity(outbox);
        outboxJpaRepository.save(outboxJpaEntity);
    }
}
