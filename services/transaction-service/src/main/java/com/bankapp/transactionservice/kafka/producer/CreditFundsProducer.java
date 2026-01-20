package com.bankapp.transactionservice.kafka.producer;

import com.bankapp.events.account.v1.CreditFunds;
import com.bankapp.events.account.v1.DebitFunds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class CreditFundsProducer {


    private static final Logger log = LoggerFactory.getLogger(DebitFundsProducer.class);
    private static final String TOPIC = "account-debit-topic";

    private final KafkaTemplate<String, CreditFunds> kafkaTemplate;

    public CreditFundsProducer(KafkaTemplate<String, CreditFunds> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDebitFundsEvent(CreditFunds event) {
        kafkaTemplate.send(TOPIC, event.getCommandId().toString(), event);
        log.info("Produced DebitFunds event: {}", event);
    }
}
