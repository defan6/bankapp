package com.bankapp.accountservice.application.port.out;

import com.bankapp.accountservice.domain.model.Outbox;

public interface OutboxRepository {

    void save(Outbox outbox);
}
