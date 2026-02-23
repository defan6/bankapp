package com.bankapp.userservice.client;

import com.bankapp.common.client.accountservice.model.BatchCreateAccountsResponse;
import com.bankapp.common.client.accountservice.model.CreateAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
    name = "account-service",
    url = "${account.service.url:http://localhost:8082}"
)
public interface AccountServiceClient {

    @PostMapping("/api/v1/accounts/batch")
    ResponseEntity<BatchCreateAccountsResponse> createAccountsBatch(@RequestBody List<CreateAccountRequest> requests);
}
