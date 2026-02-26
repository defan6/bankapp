package com.bankapp.accountservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Inbox {


    private UUID messageId;

    private LocalDateTime eventTimestamp;

    private LocalDateTime receivedAt;


    public static Inbox createInboxEvent(UUID messageId, LocalDateTime eventTimestamp) {
        return new Inbox(messageId, eventTimestamp, LocalDateTime.now());
    }

    public static Inbox of(UUID messageId, LocalDateTime eventTimestamp, LocalDateTime receivedAt) {
        return new Inbox(messageId, eventTimestamp, receivedAt);
    }
}
