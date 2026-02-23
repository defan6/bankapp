package com.bankapp.accountservice.adapters.in.web;

import com.bankapp.accountservice.application.port.in.BatchCreateAccountResult;
import com.bankapp.accountservice.application.port.in.CreateAccountCommand;
import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.common.client.accountservice.model.AccountResponse;
import com.bankapp.common.client.accountservice.model.BatchCreateAccountsResponse;
import com.bankapp.common.client.accountservice.model.CreateAccountRequest;
import com.bankapp.common.client.accountservice.model.CreateAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountWebMapper {
    CreateAccountCommand toCreateAccountCommand(CreateAccountRequest createAccountRequest);

    @Mapping(source = "balance.amount", target = "balance")
    @Mapping(source = "id", target = "accountId")
    CreateAccountResponse toCreateAccountResponse(Account account);


    @Mapping(source = "balance.amount", target = "balance")
    @Mapping(source = "id", target = "accountId")
    AccountResponse toAccountResponse(Account account);

    com.bankapp.common.client.accountservice.model.BatchAccountResult toBatchAccountResultDto(BatchCreateAccountResult result);

    default BatchCreateAccountsResponse toBatchCreateAccountsResponse(List<BatchCreateAccountResult> results) {
        if (results == null) {
            return null;
        }

        BatchCreateAccountsResponse response = new BatchCreateAccountsResponse();
        response.setTotal(results.size());

        long successCount = results.stream().filter(BatchCreateAccountResult::success).count();
        long failedCount = results.size() - successCount;

        response.setSuccessCount((int) successCount);
        response.setFailedCount((int) failedCount);

        List<com.bankapp.common.client.accountservice.model.BatchAccountResult> resultDtos = results.stream()
                .map(this::toBatchAccountResultDto)
                .toList();
        response.setResults(resultDtos);

        return response;
    }
}
