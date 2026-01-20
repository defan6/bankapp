package com.bankapp.accountservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Outbox {

    private UUID id;

    private AggregateType aggregateType;

    private String aggregateId;

    private String topic;

    private String payload;

    private Map<String, Object> headers;

    private LocalDateTime eventTimestamp;

    private LocalDateTime createdAt;

    private int retryCount;

    private LocalDateTime nextRetryAt;


    public static Outbox createOutboxEvent(
            UUID commandId,
            AggregateType aggregateType,
            String aggregateId,
            String topic,
            String payload,
            Map<String, Object> headers,
            LocalDateTime eventTimestamp,
            int retryCount,
            LocalDateTime nextRetryAt) {
        return new Outbox(
                commandId,
                aggregateType,
                aggregateId,
                topic,
                payload,
                headers,
                eventTimestamp,
                LocalDateTime.now(),
                retryCount,
                nextRetryAt
        );
    }


    public static Outbox of(
            UUID commandId,
            AggregateType aggregateType,
            String aggregateId,
            String topic,
            String payload,
            Map<String, Object> headers,
            LocalDateTime eventTimestamp,
            LocalDateTime createdAt,
            int retryCount,
            LocalDateTime nextRetryAt) {
        return new Outbox(
                commandId,
                aggregateType,
                aggregateId,
                topic,
                payload,
                headers,
                eventTimestamp,
                createdAt,
                retryCount,
                nextRetryAt
        );
    }

}
