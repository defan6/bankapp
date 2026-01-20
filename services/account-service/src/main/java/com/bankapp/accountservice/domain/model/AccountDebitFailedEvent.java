package com.bankapp.accountservice.domain.model;

import com.bankapp.accountservice.domain.marker.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record AccountDebitFailedEvent(
        UUID eventId,
        UUID accountId,
        UUID correlationId,
        Money tryToDebitAmount,
        Money balance,
        String reason,
        LocalDateTime timestamp,
        Map<String, Object> headers
) implements DomainEvent {


    public static AccountDebitFailedEvent createAccountDebitFailedEvent(
            UUID accountId,
            UUID correlationId,
            Money tryToDebitAmount,
            Money balance,
            String reason,
            Map<String, Object> headers
    ) {
        return new AccountDebitFailedEvent(
                UUID.randomUUID(),
                accountId,
                correlationId,
                tryToDebitAmount,
                balance,
                reason,
                LocalDateTime.now(),
                headers
        );
    }

    public String toJsonString(){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing AccountDebitFailedEvent to JSON", e);
        }
    }
}
