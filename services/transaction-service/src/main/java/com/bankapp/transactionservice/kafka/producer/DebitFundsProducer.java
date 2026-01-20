package com.bankapp.transactionservice.kafka.producer;

import com.bankapp.events.account.v1.DebitFunds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DebitFundsProducer {

    private static final Logger log = LoggerFactory.getLogger(DebitFundsProducer.class);
    private static final String TOPIC = "account-debit-topic";

    private final KafkaTemplate<String, DebitFunds> kafkaTemplate;

    public DebitFundsProducer(KafkaTemplate<String, DebitFunds> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDebitFundsEvent(DebitFunds event) {
        kafkaTemplate.send(TOPIC, event.getCommandId().toString(), event);
        log.info("Produced DebitFunds event: {}", event);
    }
}
