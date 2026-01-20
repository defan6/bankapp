package com.bankapp.accountservice.adapters.out.messaging.mapper;

import com.bankapp.accountservice.domain.exception.JsonParseException;
import com.bankapp.accountservice.domain.model.Outbox;
import com.bankapp.events.account.v1.FundsDebited;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OutboxToFundsDebitedMapper {

    private final ObjectMapper objectMapper;

    public OutboxToFundsDebitedMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<FundsDebited> toFundsDebitedList(List<Outbox> outboxList) throws JsonParseException {
        if (outboxList == null) {
            return null;
        }

        try {
            return outboxList.stream()
                    .map(outbox -> {
                        try {
                            return outboxToFundsDebited(outbox);
                        } catch (JsonParseException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof JsonParseException) {
                throw (JsonParseException) e.getCause();
            }
            throw e;
        }
    }

    public FundsDebited outboxToFundsDebited(Outbox outbox) throws JsonParseException{
        try {
            JsonNode jsonPayload = objectMapper.readTree(outbox.getPayload());

            return FundsDebited.newBuilder()
                    .setEventId(UUID.fromString(jsonPayload.get("eventId").asText()))
                    .setCorrelationId(UUID.fromString(jsonPayload.get("correlationId").asText()))
                    .setAccountId(UUID.fromString(jsonPayload.get("accountId").asText()))
                    .setDebitedAmount(new BigDecimal(jsonPayload.get("debitedAmount").get("amount").asText()))
                    .setNewBalance(new BigDecimal(jsonPayload.get("newBalance").get("amount").asText()))
                    .setTimestamp(parseTimestamp(jsonPayload.get("timestamp")))
                    .setHeaders(mapJsonNodeToMap(jsonPayload.get("headers")))
                    .build();

        } catch (JsonProcessingException | IllegalArgumentException | NullPointerException e) {
            throw new JsonParseException("Failed to map Outbox to FundsDebited for outbox ID: " + outbox.getId());
        }
    }


    private static Instant parseTimestamp(JsonNode node) {
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("timestamp is missing");
        }

        if (node.isTextual()) {
            return Instant.parse(node.asText());
        }

        if (node.isArray()) {
            ArrayNode a = (ArrayNode) node;

            LocalDateTime ldt = LocalDateTime.of(
                    a.get(0).asInt(),
                    a.get(1).asInt(),
                    a.get(2).asInt(),
                    a.get(3).asInt(),
                    a.get(4).asInt(),
                    a.get(5).asInt(),
                    a.get(6).asInt()
            );

            return ldt.atZone(ZoneOffset.UTC).toInstant();
        }

        throw new IllegalArgumentException("Unsupported timestamp format: " + node);
    }


    private Map<String, String> mapJsonNodeToMap(JsonNode headersNode) {
        if (headersNode == null || !headersNode.isObject()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        headersNode.fields().forEachRemaining(entry -> {
            result.put(entry.getKey(), entry.getValue().asText());
        });

        return result;
    }


}
