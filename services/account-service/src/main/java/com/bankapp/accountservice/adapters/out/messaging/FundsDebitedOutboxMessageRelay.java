package com.bankapp.accountservice.adapters.out.messaging;

import com.bankapp.accountservice.adapters.out.messaging.mapper.OutboxToFundsDebitedMapper;
import com.bankapp.accountservice.adapters.out.messaging.mapper.TimestampAndHeadersMapper;
import com.bankapp.accountservice.adapters.out.messaging.relay.AbstractOutboxMessageRelay;
import com.bankapp.accountservice.domain.exception.JsonParseException;
import com.bankapp.accountservice.domain.model.AggregateType;
import com.bankapp.accountservice.domain.model.Outbox;
import com.bankapp.events.account.v1.FundsDebited;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

@Component
public class FundsDebitedOutboxMessageRelay extends AbstractOutboxMessageRelay<FundsDebited>{

    public FundsDebitedOutboxMessageRelay(
            JdbcTemplate jdbcTemplate,
            KafkaTemplate<String, FundsDebited> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        super(jdbcTemplate, kafkaTemplate, objectMapper);
    }

    @Override
    protected FundsDebited mapToEvent(Outbox outbox) throws  JsonParseException{
        try{
            JsonNode jsonNode = objectMapper.readTree(outbox.getPayload());
            return FundsDebited.newBuilder()
                    .setEventId(UUID.fromString(jsonNode.get("eventId").asText()))
                    .setCorrelationId(UUID.fromString(jsonNode.get("correlationId").asText()))
                    .setAccountId(UUID.fromString(jsonNode.get("accountId").asText()))
                    .setDebitedAmount(new BigDecimal(jsonNode.get("debitedAmount").get("amount").asText()))
                    .setNewBalance(new BigDecimal(jsonNode.get("newBalance").get("amount").asText()))
                    .setTimestamp(TimestampAndHeadersMapper.parseTimestamp(jsonNode.get("timestamp")))
                    .setHeaders(TimestampAndHeadersMapper.mapJsonNodeToMap(jsonNode.get("headers")))
                    .build();
        } catch(JsonProcessingException e){
            throw new JsonParseException("Failed to map Outbox to FundsDebited for outbox ID: " + outbox.getId());
        }
    }

    @Override
    protected String getEventKey(FundsDebited event) {
        return event.getAccountId().toString();
    }

    @Override
    protected AggregateType getSupportedAggregateType() {
        return AggregateType.FUNDS_DEBITED;
    }

    @Override
    @Scheduled(initialDelayString = "${app.scheduler.initial-delay:10000}", fixedDelayString = "${app.scheduler.fixed-delay:5000}")
    @Transactional
    public void pollAndPublishEvents() {
        super.pollAndPublishEvents();
    }
}
