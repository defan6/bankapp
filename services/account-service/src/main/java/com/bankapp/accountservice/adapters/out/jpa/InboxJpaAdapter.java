package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.adapters.out.jpa.mapper.InboxJpaMapper;
import com.bankapp.accountservice.application.port.out.InboxRepository;
import com.bankapp.accountservice.domain.model.Inbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboxJpaAdapter implements InboxRepository {

    private final InboxJpaRepository inboxJpaRepository;

    private final InboxJpaMapper inboxJpaMapper;

    @Override
    public boolean insertIfNotExists(Inbox inbox) {
        InboxJpaEntity inboxJpaEntity = inboxJpaMapper.toInboxJpaEntity(inbox);
        return inboxJpaRepository
                .insertIfNotExists(inbox.getMessageId(), inbox.getEventTimestamp(), inbox.getReceivedAt()) > 0;
    }
}
