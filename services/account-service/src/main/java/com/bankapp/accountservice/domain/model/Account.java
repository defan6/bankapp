package com.bankapp.accountservice.domain.model;


import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    private UUID id;
    private UUID userId;
    private Money balance;
    private Currency currency;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static Account of(UUID id, UUID userId, Money balance, Currency currency,
                             Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Account(id, userId, balance, currency, version, createdAt, updatedAt);
    }


    public static Account createAccount(UUID userId, Currency currency, Notification notification) {
        Objects.requireNonNull(userId, "user id is required");
        Objects.requireNonNull(currency, "currency is required");
        return new Account(
                UUID.randomUUID(),
                userId,
                Money.zero(currency),
                currency,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }


    public void debit(Money amount, Notification notification) {
        if (amount == null) {
            notification.addError("AMOUNT_REQUIRED", "amount cannot be empty");
        }
        if (!this.currency.equals(amount.getCurrency())) {
            notification.addError("CURRENCY_MISMATCH", "it is not possible to write off funds in another currency");
        }
        if (this.balance.isLessThan(amount)) {
            notification.addError("INSUFFICIENT_FUNDS", "not enough funds in balance");
        }
        if (!notification.hasErrors()) {
            this.balance = this.balance.subtract(amount);
            // this.version++; // Управляется JPA
            // this.updatedAt = LocalDateTime.now(); // Управляется JPA
        }
    }

    public void credit(Money amount, Notification notification) {
        if (amount == null) {
            notification.addError("AMOUNT_REQUIRED", "amount cannot be empty");
        }
        if (!this.currency.equals(amount.getCurrency())) {
            notification.addError("CURRENCY_MISMATCH", "it is not possible to receive funds in another currency");
        }
        if (!notification.hasErrors()) {
            this.balance = this.balance.add(amount);
            // this.version++; // Управляется JPA
            // this.updatedAt = LocalDateTime.now(); // Управляется JPA
        }
    }


}
