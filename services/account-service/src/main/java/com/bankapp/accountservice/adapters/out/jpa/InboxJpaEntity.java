package com.bankapp.accountservice.adapters.out.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "inbox")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InboxJpaEntity {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "event_timestamp")
    private LocalDateTime eventTimestamp;


    @Column(name = "received_at")
    private LocalDateTime receivedAt;
}
