package com.bankapp.accountservice.adapters.out.jpa;

import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.accountservice.domain.model.Currency;
import com.bankapp.accountservice.domain.model.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.bankapp.accountservice.domain.model.Notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@Import({AccountJpaAdapter.class, com.bankapp.accountservice.adapters.out.jpa.mapper.AccountJpaMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountJpaAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private AccountJpaAdapter accountJpaAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Для очистки

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE accounts CASCADE");
    }

    @Test
    @DisplayName("Should save account and find it by ID")
    void saveAndFindAccount_shouldReturnSameAccount() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Account originalAccount = Account.createAccount(userId, Currency.EUR, new com.bankapp.accountservice.domain.model.Notification());

        // WHEN
        accountJpaAdapter.save(originalAccount);
        Optional<Account> foundAccountOpt = accountJpaAdapter.findAccount(originalAccount.getId());

        // THEN
        assertThat(foundAccountOpt).isPresent();
        Account foundAccount = foundAccountOpt.get();
        assertThat(foundAccount.getId()).isEqualTo(originalAccount.getId());
        assertThat(foundAccount.getUserId()).isEqualTo(userId);
        assertThat(foundAccount.getBalance().getAmount()).isEqualByComparingTo(BigDecimal.ZERO); // Initial balance is zero
        assertThat(foundAccount.getBalance().getCurrency()).isEqualTo(Currency.EUR);
    }

    @Test
    @DisplayName("Should return empty Optional when account is not found")
    void findAccount_shouldReturnEmpty_whenNotFound() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();

        // WHEN
        Optional<Account> foundAccountOpt = accountJpaAdapter.findAccount(nonExistentId);

        // THEN
        assertThat(foundAccountOpt).isNotPresent();
    }

    @Test
    @DisplayName("Should find all accounts by a specific user ID")
    void findAllAccountsByUserId_shouldReturnCorrectAccounts() {
        // GIVEN
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        Account account1_user1 = Account.createAccount(userId1, Currency.USD, new Notification());
        Account account2_user1 = Account.createAccount(userId1, Currency.EUR, new Notification());
        Account account1_user2 = Account.createAccount(userId2, Currency.USD, new Notification());

        accountJpaAdapter.save(account1_user1);
        accountJpaAdapter.save(account2_user1);
        accountJpaAdapter.save(account1_user2);

        // WHEN
        List<Account> user1Accounts = accountJpaAdapter.findAllAccountsByUserId(userId1);

        // THEN
        assertThat(user1Accounts).hasSize(2);
        assertThat(user1Accounts).extracting(Account::getId).containsExactlyInAnyOrder(account1_user1.getId(), account2_user1.getId());
    }
}
