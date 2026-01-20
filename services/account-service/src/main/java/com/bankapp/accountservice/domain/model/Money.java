package com.bankapp.accountservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Money {
    private BigDecimal amount;
    private Currency currency;

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }


    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }


    public Money add(Money other) {
        return new Money(amount.add(other.amount), currency);
    }


    public Money subtract(Money other) {
        return new Money(amount.subtract(other.amount), currency);
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }


    @Override
    public String toString() {
        return amount.setScale(currency.getDefaultScale(), RoundingMode.HALF_EVEN) + " " + currency.name();
    }
}