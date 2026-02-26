package com.bankapp.accountservice.adapters.out.jpa.mapper;


import com.bankapp.accountservice.adapters.out.jpa.InboxJpaEntity;
import com.bankapp.accountservice.domain.model.Inbox;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InboxJpaMapper {

    default Inbox toInbox(InboxJpaEntity inboxJpaEntity){
        return Inbox.of(inboxJpaEntity.getMessageId(),inboxJpaEntity.getEventTimestamp(), inboxJpaEntity.getReceivedAt());
    }

    InboxJpaEntity toInboxJpaEntity(Inbox inbox);
}
