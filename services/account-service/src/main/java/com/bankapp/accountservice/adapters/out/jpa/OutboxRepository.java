package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.domain.model.Outbox;

public interface OutboxRepository {
    Outbox save(Outbox outbox);
}
