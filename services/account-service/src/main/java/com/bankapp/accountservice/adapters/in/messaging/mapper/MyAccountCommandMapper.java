package com.bankapp.accountservice.adapters.in.messaging.mapper;

import com.bankapp.accountservice.application.port.in.CreditAccountCommand;
import com.bankapp.accountservice.application.port.in.DebitAccountCommand;
import com.bankapp.accountservice.domain.model.Currency;
import com.bankapp.events.account.v1.CreditFunds;
import com.bankapp.events.account.v1.DebitFunds;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Component
public class MyAccountCommandMapper {

    public DebitAccountCommand toDebitAccountCommand(DebitFunds debitFunds, Map<String, Object> headers){
        return new DebitAccountCommand(
                debitFunds.getCommandId(),
                debitFunds.getCorrelationId(),
                debitFunds.getAccountId(),
                debitFunds.getAmount(),
                headers,
                Currency.fromString(debitFunds.getCurrency()),
                LocalDateTime.ofInstant(debitFunds.getTimestamp(), ZoneId.systemDefault())
        );
    }

    public CreditAccountCommand toCreditAccountCommand(CreditFunds creditFunds, Map<String, Object> headers) {
        return new CreditAccountCommand(
                creditFunds.getCommandId(),
                creditFunds.getCorrelationId(),
                creditFunds.getAccountId(),
                creditFunds.getAmount(),
                headers,
                Currency.fromString(creditFunds.getCurrency()),
                LocalDateTime.ofInstant(creditFunds.getTimestamp(), ZoneId.systemDefault())
        );
    }
}
