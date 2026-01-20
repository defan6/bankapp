package com.bankapp.accountservice.adapters.out.jpa.mapper;

import com.bankapp.accountservice.adapters.out.jpa.OutboxJpaEntity;
import com.bankapp.accountservice.domain.model.Outbox;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OutboxJpaMapper {

    OutboxJpaEntity toOutboxJpaEntity(Outbox outbox);


    default Outbox toOutbox(OutboxJpaEntity outboxJpaEntity) {
        return Outbox.of(outboxJpaEntity.getId(),
                outboxJpaEntity.getAggregateType(),
                outboxJpaEntity.getAggregateId(),
                outboxJpaEntity.getTopic(),
                outboxJpaEntity.getPayload(),
                outboxJpaEntity.getHeaders(),
                outboxJpaEntity.getEventTimestamp(),
                outboxJpaEntity.getCreatedAt(),
                outboxJpaEntity.getRetryCount(),
                outboxJpaEntity.getNextRetryAt()
        );
    }
}
