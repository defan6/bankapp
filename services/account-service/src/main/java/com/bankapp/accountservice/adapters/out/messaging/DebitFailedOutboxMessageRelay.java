package com.bankapp.accountservice.adapters.out.messaging;

import com.bankapp.accountservice.adapters.out.messaging.mapper.TimestampAndHeadersMapper;
import com.bankapp.accountservice.adapters.out.messaging.relay.AbstractOutboxMessageRelay;
import com.bankapp.accountservice.domain.exception.JsonParseException;
import com.bankapp.accountservice.domain.model.AggregateType;
import com.bankapp.accountservice.domain.model.Outbox;
import com.bankapp.events.account.v1.DebitFailed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DebitFailedOutboxMessageRelay extends AbstractOutboxMessageRelay<DebitFailed> {


    public DebitFailedOutboxMessageRelay(JdbcTemplate jdbcTemplate, KafkaTemplate<String, DebitFailed> kafkaTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, kafkaTemplate, objectMapper);
    }

    @Override
    protected DebitFailed mapToEvent(Outbox outbox) throws JsonParseException {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(outbox.getPayload());
            JsonNode balanceNode = jsonNode.get("balance");
            BigDecimal balance = (balanceNode != null && !balanceNode.isNull())
                    ? new BigDecimal(balanceNode.get("amount").asText())
                    : BigDecimal.ZERO;

            return DebitFailed.newBuilder()
                    .setEventId(UUID.fromString(jsonNode.get("eventId").asText()))
                    .setAccountId(UUID.fromString(jsonNode.get("accountId").asText()))
                    .setCorrelationId(UUID.fromString(jsonNode.get("correlationId").asText()))
                    .setReason(jsonNode.get("reason").asText())
                    .setTimestamp(TimestampAndHeadersMapper.parseTimestamp(jsonNode.get("timestamp")))
                    .setHeaders(TimestampAndHeadersMapper.mapJsonNodeToMap(jsonNode.get("headers")))
                    .setTryToDebitAmount(new BigDecimal(jsonNode.get("tryToDebitAmount").get("amount").asText()))
                    .setBalance(balance)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getEventKey(DebitFailed event) {
        return event.getAccountId().toString();
    }

    @Override
    protected AggregateType getSupportedAggregateType() {
        return AggregateType.DEBIT_FAILED;
    }


    @Override
    @Scheduled(initialDelayString = "${app.scheduler.initial-delay:10000}", fixedDelayString = "${app.scheduler.fixed-delay:5000}")
    @Transactional
    public void pollAndPublishEvents() {
        super.pollAndPublishEvents();
    }
}
