package com.bankapp.accountservice.application.service;

import com.bankapp.accountservice.application.port.in.CreditAccountCommand;
import com.bankapp.accountservice.application.port.in.DebitAccountCommand;
import com.bankapp.accountservice.application.port.out.AccountRepository;
import com.bankapp.accountservice.application.port.out.EventPublisher;
import com.bankapp.accountservice.domain.exception.DomainValidationException;
import com.bankapp.accountservice.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAccountServiceTest {

    @InjectMocks
    private DefaultAccountService defaultAccountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Nested
    @DisplayName("Tests for processDebitFunds method")
    class ProcessDebitFundsTests {

        @Test
        @DisplayName("Should debit account and publish event when funds are sufficient")
        void processDebitFunds_shouldDebitAccount_whenFundsAreSufficient() {
            // GIVEN
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            DebitAccountCommand command = new DebitAccountCommand(UUID.randomUUID(), UUID.randomUUID(), accountId, new BigDecimal("50.00"), null, Currency.USD, null);

            Account initialAccount = Account.of(
                    accountId,
                    userId,
                    Money.of(new BigDecimal("100.00"), Currency.USD),
                    Currency.USD,
                    1L,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            // Настраиваем мок репозитория
            when(accountRepository.findAccount(accountId)).thenReturn(Optional.of(initialAccount));

            // WHEN
            defaultAccountService.processDebitFunds(command);

            // THEN
            // Проверяем, что аккаунт был сохранен
            verify(accountRepository, times(1)).save(any(Account.class));

            // Проверяем, что событие было опубликовано
            verify(eventPublisher, times(1)).publish(any(AccountDebitedEvent.class));

            // (Опционально) Проверяем, что баланс уменьшился
            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getBalance().getAmount()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Should throw DomainValidationException when funds are insufficient")
        void processDebitFunds_shouldThrowException_whenFundsAreInsufficient() {
            // GIVEN
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            DebitAccountCommand command = new DebitAccountCommand(UUID.randomUUID(), UUID.randomUUID(), accountId, new BigDecimal("150.00"), null, Currency.USD, null);

            Account initialAccount = Account.of(
                    accountId,
                    userId,
                    Money.of(new BigDecimal("100.00"), Currency.USD),
                    Currency.USD,
                    1L,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(accountRepository.findAccount(accountId)).thenReturn(Optional.of(initialAccount));

            // WHEN & THEN
            assertThrows(DomainValidationException.class, () -> {
                defaultAccountService.processDebitFunds(command);
            });

            // Убедимся, что ничего не сохранялось и не публиковалось
            verify(accountRepository, never()).save(any(Account.class));
            verify(eventPublisher, never()).publish(any(AccountDebitedEvent.class));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when debit currency is different from account currency")
        void processDebitFunds_shouldThrowException_whenCurrencyIsDifferent() {
            // GIVEN
            UUID accountId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            DebitAccountCommand command = new DebitAccountCommand(UUID.randomUUID(), UUID.randomUUID(), accountId, new BigDecimal("50.00"), null, Currency.EUR, null);

            Account initialAccount = Account.of(
                    accountId,
                    userId,
                    Money.of(new BigDecimal("100.00"), Currency.USD),
                    Currency.USD,
                    1L,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(accountRepository.findAccount(accountId)).thenReturn(Optional.of(initialAccount));

            // WHEN & THEN
            assertThrows(DomainValidationException.class, () -> {
                defaultAccountService.processDebitFunds(command);
            });

            verify(accountRepository, never()).save(any(Account.class));
            verify(eventPublisher, never()).publish(any(AccountDebitedEvent.class));
        }

        @Test
        @DisplayName("Should throw DomainValidationException when account is not found")
        void processDebitFunds_shouldThrowException_whenAccountNotFound() {
            // GIVEN
            UUID nonExistentAccountId = UUID.randomUUID();
            DebitAccountCommand command = new DebitAccountCommand(UUID.randomUUID(), UUID.randomUUID(), nonExistentAccountId, new BigDecimal("50.00"), null, Currency.USD, null);

            when(accountRepository.findAccount(nonExistentAccountId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThrows(DomainValidationException.class, () -> {
                defaultAccountService.processDebitFunds(command);
            });

            verify(accountRepository, never()).save(any(Account.class));
            verify(eventPublisher, never()).publish(any(AccountDebitedEvent.class));
        }
        @Nested
        @DisplayName("Tests for processCreditFunds method")
        class ProcessCreditFundsTests {
            @Test
            @DisplayName("Should credit account and publish event when funds are credited successfully")
            void processCreditFunds_shouldCreditAccount_whenFundsAreSufficient() {
                // GIVEN
                UUID accountId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                CreditAccountCommand command = new CreditAccountCommand(UUID.randomUUID(), UUID.randomUUID(), accountId, new BigDecimal("50.00"), null, Currency.USD, null);

                Account initialAccount = Account.of(
                        accountId,
                        userId,
                        Money.of(new BigDecimal("100.00"), Currency.USD),
                        Currency.USD,
                        1L,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

                when(accountRepository.findAccount(accountId)).thenReturn(Optional.of(initialAccount));

                // WHEN
                defaultAccountService.processCreditFunds(command);

                // THEN
                verify(accountRepository, times(1)).save(any(Account.class));
                verify(eventPublisher, times(1)).publish(any(AccountCreditedEvent.class));

                ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
                verify(accountRepository).save(accountCaptor.capture());
                assertThat(accountCaptor.getValue().getBalance().getAmount()).isEqualByComparingTo("150.00");
            }
            @Test
            @DisplayName("Should throw DomainValidationException when credit currency is different from account currency")
            void processCreditFunds_shouldThrowException_whenCurrencyIsDifferent() {
                // GIVEN
                UUID accountId = UUID.randomUUID();
                CreditAccountCommand command = new CreditAccountCommand(UUID.randomUUID(), UUID.randomUUID(), accountId, new BigDecimal("50.00"), null, Currency.EUR, null);

                Account initialAccount = Account.of(
                        accountId,
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("100.00"), Currency.USD),
                        Currency.USD,
                        1L,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

                when(accountRepository.findAccount(accountId)).thenReturn(Optional.of(initialAccount));

                // WHEN & THEN
                assertThrows(DomainValidationException.class, () -> {
                    defaultAccountService.processCreditFunds(command);
                });

                verify(accountRepository, never()).save(any(Account.class));
                verify(eventPublisher, never()).publish(any(AccountDebitedEvent.class));
            }

            @Test
            @DisplayName("Should throw DomainValidationException when account is not found")
            void processCreditFunds_shouldThrowException_whenAccountNotFound() {
                // GIVEN
                UUID nonExistentAccountId = UUID.randomUUID();
                CreditAccountCommand command = new CreditAccountCommand(UUID.randomUUID(), UUID.randomUUID(), nonExistentAccountId, new BigDecimal("50.00"), null, Currency.USD, null);

                when(accountRepository.findAccount(nonExistentAccountId)).thenReturn(Optional.empty());

                // WHEN & THEN
                assertThrows(DomainValidationException.class, () -> {
                    defaultAccountService.processCreditFunds(command);
                });

                verify(accountRepository, never()).save(any(Account.class));
                verify(eventPublisher, never()).publish(any(AccountDebitedEvent.class));
            }
        }
    }
}
