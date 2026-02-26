package com.bankapp.accountservice.adapters.out.messaging;

import com.bankapp.accountservice.adapters.out.messaging.mapper.TimestampAndHeadersMapper;
import com.bankapp.accountservice.adapters.out.messaging.relay.AbstractOutboxMessageRelay;
import com.bankapp.accountservice.domain.exception.JsonParseException;
import com.bankapp.accountservice.domain.model.AggregateType;
import com.bankapp.accountservice.domain.model.Outbox;
import com.bankapp.events.account.v1.FundsCredited;
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
public class FundsCreditedOutboxMessageRelay extends AbstractOutboxMessageRelay<FundsCredited> {

    public FundsCreditedOutboxMessageRelay(JdbcTemplate jdbcTemplate, KafkaTemplate<String, FundsCredited> kafkaTemplate, ObjectMapper objectMapper) {
        super(jdbcTemplate, kafkaTemplate, objectMapper);
    }

    @Override
    protected FundsCredited mapToEvent(Outbox outbox) throws JsonParseException {
        try {
            JsonNode jsonNode = objectMapper.readTree(outbox.getPayload());
            return FundsCredited.newBuilder()
                    .setEventId(UUID.fromString(jsonNode.get("eventId").asText()))
                    .setCorrelationId(UUID.fromString(jsonNode.get("correlationId").asText()))
                    .setAccountId(UUID.fromString(jsonNode.get("accountId").asText()))
                    .setCreditedAmount(new BigDecimal(jsonNode.get("creditedAmount").get("amount").asText()))
                    .setNewBalance(new BigDecimal(jsonNode.get("newBalance").get("amount").asText()))
                    .setTimestamp(TimestampAndHeadersMapper.parseTimestamp(jsonNode.get("timestamp")))
                    .setHeaders(TimestampAndHeadersMapper.mapJsonNodeToMap(jsonNode.get("headers")))
                    .build();
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Failed to map Outbox to FundsDebited for outbox ID: " + outbox.getId());
        }
    }

    @Override
    protected String getEventKey(FundsCredited event) {
        return event.getAccountId().toString();
    }

    @Override
    protected AggregateType getSupportedAggregateType() {
        return AggregateType.FUNDS_CREDITED;
    }


    @Override
    @Scheduled(initialDelayString = "${app.scheduler.initial-delay:10000}", fixedDelayString = "${app.scheduler.fixed-delay:5000}")
    @Transactional
    public void pollAndPublishEvents() {
        super.pollAndPublishEvents();
    }
}
