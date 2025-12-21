package com.bankapp.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

// Интерфейс для Feign Client, который будет обращаться к Account Service
// 'account-service' - это логическое имя сервиса (будет использоваться в Docker Compose или Eureka)
// path - префикс для всех запросов этого клиента
@FeignClient(name = "account-service", path = "/api/accounts")
public interface AccountServiceClient {

    // Класс-репрезентация ответа от Account Service
    class AccountDetailsDto {
        private UUID accountId;
        private UUID userId;
        private BigDecimal balance;
        private String currency;

        // Getters and setters
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    @GetMapping("/{accountId}")
    AccountDetailsDto getAccountDetails(@PathVariable("accountId") UUID accountId);
}
