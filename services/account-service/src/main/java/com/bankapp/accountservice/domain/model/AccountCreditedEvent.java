package com.bankapp.accountservice.domain.model;

import com.bankapp.accountservice.domain.marker.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountCreditedEvent implements DomainEvent {
    private UUID eventId;
    private UUID accountId;
    private UUID correlationId;
    private Money creditedAmount;
    private Money newBalance;
    private LocalDateTime timestamp;
    private Map<String, Object> headers;


    public static AccountCreditedEvent createAccountCreditedEvent(
            UUID accountId,
            UUID correlationId,
            Money creditedAmount,
            Money newBalance,
            Map<String, Object> headers
    ) {
        return new AccountCreditedEvent(
                UUID.randomUUID(),
                accountId,
                correlationId,
                creditedAmount,
                newBalance,
                LocalDateTime.now(),
                headers
        );
    }


    public String toJsonString(){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
