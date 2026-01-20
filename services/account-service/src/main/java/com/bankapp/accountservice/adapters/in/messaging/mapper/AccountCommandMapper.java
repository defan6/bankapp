package com.bankapp.accountservice.adapters.in.messaging.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountCommandMapper {

//    @Mapping(target = "currency", expression = "java(mapCurrency(debitFunds.getCurrency()))")
//    DebitAccountCommand toDebitAccountCommand(DebitFunds debitFunds, MultiValueMap<String, String> headers);
//
//    @Mapping(target = "currency", expression = "java(mapCurrency(creditFunds.getCurrency()))")
//    CreditAccountCommand toCreditAccountCommand(CreditFunds creditFunds);
//
//    default Currency mapCurrency(String currencyCode) {
//        if (currencyCode == null) {
//            return null;
//        }
//        return Currency.fromString(currencyCode);
//    }
}
