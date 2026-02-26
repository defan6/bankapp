package com.bankapp.accountservice.adapters.in.web;


import com.bankapp.accountservice.application.port.in.CreateAccountCommand;
import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.common.client.accountservice.model.AccountResponse;
import com.bankapp.common.client.accountservice.model.CreateAccountRequest;
import com.bankapp.common.client.accountservice.model.CreateAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountWebMapper {
    CreateAccountCommand toCreateAccountCommand(CreateAccountRequest createAccountRequest);

    @Mapping(source = "balance.amount", target = "balance")
    @Mapping(source = "id", target = "accountId")
    CreateAccountResponse toCreateAccountResponse(Account account);


    @Mapping(source = "balance.amount", target = "balance")
    @Mapping(source = "id", target = "accountId")
    AccountResponse toAccountResponse(Account account);
}
