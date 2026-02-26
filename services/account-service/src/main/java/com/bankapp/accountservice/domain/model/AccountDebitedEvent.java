package com.bankapp.accountservice.domain.model;


import com.bankapp.accountservice.domain.marker.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountDebitedEvent implements DomainEvent {
    private UUID eventId;
    private UUID accountId;
    private UUID correlationId;
    private Money debitedAmount;
    private Money newBalance;
    private LocalDateTime timestamp;
    private Map<String, Object> headers;


    public static AccountDebitedEvent createAccountDebitedEvent(
            UUID accountId,
            UUID correlationId,
            Money debitedAmount,
            Money newBalance,
            Map<String, Object> headers
    ) {
        return new AccountDebitedEvent(
                UUID.randomUUID(),
                accountId,
                correlationId,
                debitedAmount,
                newBalance,
                LocalDateTime.now(),
                headers
        );
    }

    public String toJsonString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
