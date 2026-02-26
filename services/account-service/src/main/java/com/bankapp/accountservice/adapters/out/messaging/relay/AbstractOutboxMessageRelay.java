package com.bankapp.accountservice.adapters.out.messaging.relay;

import com.bankapp.accountservice.domain.exception.JsonParseException;
import com.bankapp.accountservice.domain.model.AggregateType;
import com.bankapp.accountservice.domain.model.Outbox;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j
public abstract class
AbstractOutboxMessageRelay<E> {

    protected final JdbcTemplate jdbcTemplate;
    protected final KafkaTemplate<String, E> kafkaTemplate;

    protected final ObjectMapper objectMapper;

    @Value("${app.scheduler.outbox.enabled:true}")
    private boolean schedulerEnabled;

    private static final int MAX_RETRIES = 5;

    private static final int INITIAL_RETRY_DELAY_SECONDS = 5;

    private static final String SELECT_UNPUBLISHED_EVENTS = """
            SELECT id, aggregate_type, aggregate_id, topic, payload, headers, event_timestamp, created_at, retry_count, next_retry_at
            FROM outbox
            WHERE (next_retry_at IS NULL OR next_retry_at <= NOW())
            AND AGGREGATE_TYPE = ?
            ORDER BY created_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """;


    private static final String DELETE_OUTBOX_EVENT = """
            DELETE from outbox WHERE id = ?
            """;


    private static final String UPDATE_RETRY_FIELDS = """
            UPDATE outbox SET retry_count = ?, next_retry_at = ? WHERE id = ?
            """;


    private static final String INSERT_DEAD_LETTER = """
                   INSERT INTO outbox_dead_letters (id, outbox_id, aggregate_type, aggregate_id, topic, payload, headers, event_timestamp, failed_at, error_message, stack_trace)                                                         
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)                                                                                                                                                                               
            """;


    protected abstract E mapToEvent(Outbox outbox) throws JsonParseException;

    protected abstract String getEventKey(E event);

    protected abstract AggregateType getSupportedAggregateType();

    public void pollAndPublishEvents() {
        if (!schedulerEnabled) {
            return;
        }

        List<Outbox> unpublishedEvents = jdbcTemplate.query(SELECT_UNPUBLISHED_EVENTS,
                new AbstractOutboxMessageRelay.OutboxEventRowMapper(objectMapper),
                getSupportedAggregateType().name()
        );

        if (unpublishedEvents.isEmpty()) {
            return;
        }


        log.info("Found {} new outbox events to publish.", unpublishedEvents.size());

        for (Outbox outbox : unpublishedEvents) {
            try {
                E event = mapToEvent(outbox);
                publishEvent(event, outbox.getTopic(), getEventKey(event), outbox.getHeaders() );
                jdbcTemplate.update(DELETE_OUTBOX_EVENT, outbox.getId());
                log.info("Successfully published and deleted outbox event ID {} from main outbox.", outbox.getId());
            } catch (Exception e) {
                Throwable cause = (e instanceof ExecutionException) ? e.getCause() : e;

                if (cause instanceof JsonProcessingException || cause instanceof SerializationException) {
                    log.error("NON-RETRIABLE: Outbox event ID: {} failed due to data error. Moving to dead-letter table", outbox.getId(), cause);

                    moveToDeadLetterTable(outbox, e);
                    jdbcTemplate.update(DELETE_OUTBOX_EVENT, outbox.getId());
                } else {
                    int newRetryCount = outbox.getRetryCount() + 1;
                    LocalDateTime nextRetryAt = calculateNextRetryTime(newRetryCount);
                    if (newRetryCount > MAX_RETRIES) {
                        log.error("RETRIABLE FAILED (MAX RETRIES): Outbox event ID: {} reached max retries." +
                                "Moving to dead-letter-table.", outbox.getId(), e);
                        moveToDeadLetterTable(outbox, e);
                        jdbcTemplate.update(DELETE_OUTBOX_EVENT, outbox.getId());
                    } else {
                        log.warn("RETRIABLE: Outbox event ID: {} failed to publish (attempt {}). Will retry at {}",
                                outbox.getId(), newRetryCount, nextRetryAt, e);
                        jdbcTemplate.update(UPDATE_RETRY_FIELDS, newRetryCount, Timestamp.valueOf(nextRetryAt), outbox.getId());
                    }
                }
            }
        }
    }

    private void publishEvent(E event, String topic, String key, Map<String, Object> headers) throws ExecutionException, InterruptedException {
        var producerRecord = new ProducerRecord<>(topic, null, key, event);

        headers.forEach((hkey, hvalue) -> {
            producerRecord.headers().add(hkey, hvalue.toString().getBytes());
        });

        kafkaTemplate.send(producerRecord).get();
    }


    private LocalDateTime calculateNextRetryTime(int retryCount) {
        long delay = AbstractOutboxMessageRelay.INITIAL_RETRY_DELAY_SECONDS * (long) Math.pow(2, retryCount - 1);
        return LocalDateTime.now().plus(delay, ChronoUnit.SECONDS);
    }


    private void moveToDeadLetterTable(Outbox outbox, Exception originalException) {
        try {
            String headersJson = outbox.getHeaders() != null ? objectMapper.writeValueAsString(outbox.getHeaders()) : null;
            String stackTrace = org.springframework.util.StringUtils
                    .hasText(originalException.getMessage()) ?
                    originalException.getMessage() :
                    "No message. Full stacktrace in logs.";
            jdbcTemplate.update(INSERT_DEAD_LETTER,
                    UUID.randomUUID(),
                    outbox.getId(),
                    outbox.getAggregateType().name(),
                    outbox.getAggregateId(),
                    outbox.getTopic(),
                    outbox.getPayload(),
                    headersJson,
                    Timestamp.valueOf(outbox.getEventTimestamp()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    originalException.getMessage(),
                    stackTrace
            );
        } catch (JsonProcessingException e) {
            jdbcTemplate.update(INSERT_DEAD_LETTER,
                    UUID.randomUUID(),
                    outbox.getId(),
                    outbox.getAggregateType(),
                    outbox.getAggregateId(),
                    outbox.getTopic(),
                    outbox.getPayload(),
                    "{}",
                    Timestamp.valueOf(outbox.getEventTimestamp()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    "Failed to serialize original headers: " + e.getMessage(),
                    originalException.getMessage()
            );

        }
    }


    @RequiredArgsConstructor
    private static class OutboxEventRowMapper implements RowMapper<Outbox> {

        private final ObjectMapper objectMapper;

        @Override
        public Outbox mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String headersJson = rs.getString("headers");

                Map<String, Object> headers =
                        headersJson == null
                                ? null
                                : objectMapper.readValue(headersJson, Map.class);


                OffsetDateTime eventTs =
                        rs.getObject("event_timestamp", OffsetDateTime.class);

                OffsetDateTime createdAt =
                        rs.getObject("created_at", OffsetDateTime.class);

                OffsetDateTime nextRetryAt =
                        rs.getObject("next_retry_at", OffsetDateTime.class);

                return Outbox.of(
                        rs.getObject("id", UUID.class),
                        AggregateType.valueOf(rs.getString("aggregate_type")),
                        rs.getString("aggregate_id"),
                        rs.getString("topic"),
                        rs.getString("payload"),
                        headers,
                        eventTs != null ? eventTs.toLocalDateTime() : null,
                        createdAt != null ? createdAt.toLocalDateTime() : null,
                        rs.getInt("retry_count"),
                        nextRetryAt != null ? nextRetryAt.toLocalDateTime() : null
                );
            } catch (Exception e) {
                throw new SQLException("Failed to deserialize headers json", e);
            }
        }
    }


}
