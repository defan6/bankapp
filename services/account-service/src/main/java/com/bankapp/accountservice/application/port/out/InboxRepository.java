package com.bankapp.accountservice.application.port.out;

import com.bankapp.accountservice.domain.model.Inbox;

public interface
InboxRepository {

    boolean insertIfNotExists(Inbox inbox);
}
