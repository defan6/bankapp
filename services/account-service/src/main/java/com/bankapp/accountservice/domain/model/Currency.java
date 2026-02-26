package com.bankapp.accountservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Currency {
    RUB(2),
    EUR(2),
    USD(2);

    private final int defaultScale;


    public static Currency fromString(String currency) {
        for (Currency curr : Currency.values()) {
            if (curr.name().equalsIgnoreCase(currency)) {
                return curr;
            }
        }
        throw new IllegalArgumentException("Unknown currency: " + currency);
    }


}
