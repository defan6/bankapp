package com.bankapp.accountservice.adapters.out.jpa.mapper;


import com.bankapp.accountservice.domain.model.*;
import org.mapstruct.Mapper;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OutboxEventPublisherMapper {

    default Outbox toOutbox(AccountDebitedEvent accountDebitedEvent){
        return Outbox.of(
                accountDebitedEvent.getEventId(),
                AggregateType.FUNDS_DEBITED,
                accountDebitedEvent.getAccountId().toString(),
                "account-debited-topic",
                accountDebitedEvent.toJsonString(),
                accountDebitedEvent.getHeaders(),
                accountDebitedEvent.getTimestamp(),
                LocalDateTime.now(),
                0,
                null
        );
    }


    default Outbox toOutbox(AccountDebitFailedEvent accountDebitFailedEvent){
        return Outbox.of(
                accountDebitFailedEvent.eventId(),
                AggregateType.DEBIT_FAILED,
                accountDebitFailedEvent.accountId().toString(),
                "account-debit-failed-topic",
                accountDebitFailedEvent.toJsonString(),
                accountDebitFailedEvent.headers(),
                accountDebitFailedEvent.timestamp(),
                LocalDateTime.now(),
                0,
                null

        );
    }


    default Outbox toOutbox(AccountCreditedEvent accountCreditedEvent){
        return Outbox.of(
                accountCreditedEvent.getEventId(),
                AggregateType.FUNDS_CREDITED,
                accountCreditedEvent.getAccountId().toString(),
                "account-credited-topic",
                accountCreditedEvent.toJsonString(),
                accountCreditedEvent.getHeaders(),
                accountCreditedEvent.getTimestamp(),
                LocalDateTime.now(),
                0,
                null
        );
    }


    default Outbox toOutbox(AccountCreditFailedEvent accountCreditFailedEvent){
        return Outbox.of(
                accountCreditFailedEvent.accountId(),
                AggregateType.CREDIT_FAILED,
                accountCreditFailedEvent.accountId().toString(),
                "account-credit-failed-topic",
                accountCreditFailedEvent.toJsonString(),
                accountCreditFailedEvent.headers(),
                accountCreditFailedEvent.timestamp(),
                LocalDateTime.now(),
                0,
                null
        );
    }

    default Map<String, Object> mapHeaders(MultiValueMap<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }

        return headers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size() == 1
                                ? e.getValue().get(0)
                                : e.getValue()
                ));
    }
}
