package com.bankapp.userservice.service;

import com.bankapp.common.client.accountservice.model.BatchAccountResult;
import com.bankapp.common.client.accountservice.model.BatchCreateAccountsResponse;
import com.bankapp.common.client.accountservice.model.CreateAccountRequest;
import com.bankapp.userservice.client.AccountServiceClient;
import com.bankapp.userservice.domain.User;
import com.bankapp.userservice.domain.UserAccountPending;
import com.bankapp.userservice.repository.UserAccountPendingRepository;
import com.bankapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountSyncService {

    private final UserAccountPendingRepository pendingRepository;
    private final UserRepository userRepository;
    private final AccountServiceClient accountServiceClient;

    @Value("${account.sync.batch-size:50}")
    private int batchSize;

    @Value("${account.sync.delay:10000}")
    private int delaySeconds;

    /**
     * Добавляет пользователя в очередь на создание аккаунта
     */
    @Transactional
    public void scheduleAccountCreation(User user) {
        if (!user.isAccountCreated() && !pendingRepository.existsByUserId(user.getId())) {
            UserAccountPending pending = new UserAccountPending();
            pending.setUserId(user.getId());
            pending.setEmail(user.getEmail());
            pending.setCurrency("USD"); // Default currency
            pendingRepository.save(pending);
            log.info("User {} scheduled for account creation", user.getId());
        }
    }

    /**
     * Планировщик: раз в N секунд отправляет пачку запросов на создание аккаунтов
     */
    @Scheduled(fixedDelayString = "${account.sync.delay:10000}")
    @Transactional
    public void processPendingAccounts() {
        log.debug("Starting scheduled account creation task");

        List<UserAccountPending> pendingUsers = pendingRepository.findAll()
                .stream()
                .limit(batchSize)
                .toList();

        if (pendingUsers.isEmpty()) {
            log.debug("No pending users for account creation");
            return;
        }

        log.info("Processing {} pending users for account creation", pendingUsers.size());

        // Формируем запросы
        List<CreateAccountRequest> requests = pendingUsers.stream()
                .map(this::toCreateAccountRequest)
                .toList();

        // Сохраняем userId для удаления после обработки
        List<UUID> processedUserIds = pendingUsers.stream()
                .map(UserAccountPending::getUserId)
                .toList();

        try {
            BatchCreateAccountsResponse response = accountServiceClient.createAccountsBatch(requests)
                    .getBody();

            if (response != null) {
                log.info("Batch account creation completed: total={}, success={}, failed={}",
                        response.getTotal(), response.getSuccessCount(), response.getFailedCount());

                response.getResults().forEach(result -> {
                    if (Boolean.TRUE.equals(result.getSuccess())) {
                        markUserAccountAsCreated(result.getUserId());
                    } else {
                        log.warn("Failed to create account for user {}: {}",
                                result.getUserId(), result.getError());
                    }
                });
            }

        } catch (Exception e) {
            log.error("Error during batch account creation: {}", e.getMessage(), e);
        } finally {
            // Всегда удаляем обработанные записи из pending таблицы
            pendingRepository.deleteByUserIdIn(processedUserIds);
            log.info("Deleted {} pending users from queue", processedUserIds.size());
        }
    }

    private CreateAccountRequest toCreateAccountRequest(UserAccountPending pending) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(pending.getUserId());
        request.setCurrency(pending.getCurrency());
        return request;
    }

    private void markUserAccountAsCreated(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setAccountCreated(true);
            userRepository.save(user);
            log.info("Marked user {} account as created", userId);
        });
    }
}
