package com.bankapp.accountservice.adapters.out.messaging.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;


@Component
public class TimestampAndHeadersMapper {


    public static Instant parseTimestamp(JsonNode node) {
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


    public static Map<String, String> mapJsonNodeToMap(JsonNode headersNode) {
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
