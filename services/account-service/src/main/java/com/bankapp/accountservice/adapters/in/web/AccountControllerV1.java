package com.bankapp.accountservice.adapters.in.web;

import com.bankapp.accountservice.application.port.in.CreateAccountCommand;
import com.bankapp.accountservice.application.port.in.CreateAccountUseCase;
import com.bankapp.accountservice.application.port.in.GetAccountQuery;
import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.common.client.accountservice.api.AccountApiV1;
import com.bankapp.common.client.accountservice.model.AccountResponse;
import com.bankapp.common.client.accountservice.model.CreateAccountRequest;
import com.bankapp.common.client.accountservice.model.CreateAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AccountControllerV1 implements AccountApiV1 {

    private final CreateAccountUseCase createAccountUseCase;

    private final GetAccountQuery getAccountQuery;

    private final AccountWebMapper accountWebMapper;


    @Override
    public ResponseEntity<CreateAccountResponse> createAccount(CreateAccountRequest createAccountRequest) {

        CreateAccountCommand createAccountCommand = accountWebMapper.toCreateAccountCommand(createAccountRequest);


        Account account = createAccountUseCase.createAccount(createAccountCommand);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()   // /api/accounts
                .path("/{id}")
                .buildAndExpand(account.getId())
                .toUri();
        CreateAccountResponse createAccountResponse = accountWebMapper.toCreateAccountResponse(account);
        return ResponseEntity.created(location).body(createAccountResponse);
    }

    @Override
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable("accountId") UUID accountId) {
        Account account = getAccountQuery.getAccount(accountId);
        return ResponseEntity.ok(accountWebMapper.toAccountResponse(account));
    }

    @Override
    public ResponseEntity<List<AccountResponse>> getUserAccounts(@RequestParam("userId") UUID userId) {
        List<AccountResponse> response = getAccountQuery.getUserAccounts(userId)
                .stream()
                .map(accountWebMapper::toAccountResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
