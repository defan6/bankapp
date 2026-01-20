package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.domain.model.AggregateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OutboxJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "aggregate_type")
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(name = "headers", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> headers;

    @Column(name = "event_timestamp")
    private LocalDateTime eventTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "retry_count")
    private int retryCount;


    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

}
