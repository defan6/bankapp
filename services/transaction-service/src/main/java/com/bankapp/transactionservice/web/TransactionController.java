package com.bankapp.transactionservice.web;

import com.bankapp.events.account.v1.DebitFunds;
import com.bankapp.transactionservice.kafka.producer.DebitFundsProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final DebitFundsProducer debitFundsProducer;

    public TransactionController(DebitFundsProducer debitFundsProducer) {
        this.debitFundsProducer = debitFundsProducer;
    }

    @PostMapping("/debit-funds")
    public ResponseEntity<String> sendDebitFundsEvent(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestParam UUID commandId,
            @RequestParam UUID correlationId) {

        DebitFunds debitFunds = DebitFunds.newBuilder()
                .setCommandId(commandId)
                .setCorrelationId(correlationId) // You might want to pass this from the request as well
                .setAccountId(accountId)
                .setAmount(amount)
                .setCurrency("USD") // Or pass as a request parameter
                .setTimestamp(Instant.now())
                .build();

        debitFundsProducer.sendDebitFundsEvent(debitFunds);

        return ResponseEntity.ok("DebitFunds event sent successfully!");
    }


    @PostMapping("/credit-funds")
    public ResponseEntity<String> sendCreditFundsEvent(
            @RequestParam UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestParam UUID commandId,
            @RequestParam UUID correlationId
    ) {

        DebitFunds debitFunds = DebitFunds.newBuilder()
                .setCommandId(commandId)
                .setCorrelationId(correlationId) // You might want to pass this from the request as well
                .setAccountId(accountId)
                .setAmount(amount)
                .setCurrency("USD") // Or pass as a request parameter
                .setTimestamp(Instant.now())
                .build();

        debitFundsProducer.sendDebitFundsEvent(debitFunds);

        return ResponseEntity.ok("DebitFunds event sent successfully!");
    }
}
