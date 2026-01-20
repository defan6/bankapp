package com.bankapp.accountservice.application.port.out;

import com.bankapp.accountservice.domain.model.AccountCreditFailedEvent;
import com.bankapp.accountservice.domain.model.AccountCreditedEvent;
import com.bankapp.accountservice.domain.model.AccountDebitFailedEvent;
import com.bankapp.accountservice.domain.model.AccountDebitedEvent;

public interface EventPublisher {

    void publish(AccountDebitedEvent accountDebitedEvent);

    void publish(AccountDebitFailedEvent accountDebitFailedEvent);


    void publish(AccountCreditedEvent accountCreditedEvent);

    void publish(AccountCreditFailedEvent accountCreditFailedEvent);


}
