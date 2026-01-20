package com.bankapp.accountservice;

import com.bankapp.accountservice.application.port.out.AccountRepository;
import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.accountservice.domain.model.Currency;
import com.bankapp.accountservice.domain.model.Money;
import com.bankapp.accountservice.domain.model.Notification;
import com.bankapp.events.account.v1.*;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "app.scheduler.outbox.enabled=true",
        "spring.liquibase.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.properties.schema.registry.url=mock://test-scope"
})
// Указываем явно, что нам нужен бин брокера
@EmbeddedKafka(
        topics = {"account-debit-topic", "account-debited-topic", "account-debit-failed-topic", "account-credit-topic", "account-credited-topic", "account-credit-failed-topic"},
        partitions = 1
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
class AccountCommandConsumerTest {

    private static final Logger log = LoggerFactory.getLogger(AccountCommandConsumerTest.class);
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private static final String DEBIT_TOPIC = "account-debit-topic";
    private static final String DEBITED_TOPIC = "account-debited-topic";
    private static final String DEBIT_FAILED_TOPIC = "account-debit-failed-topic";

    private static final String CREDIT_TOPIC = "account-credit-topic";
    private static final String CREDITED_TOPIC = "account-credited-topic";
    private static final String CREDIT_FAILED_TOPIC = "account-credit-failed-topic";

    // Используем @SuppressWarnings, если IDEA ругается, так как бин создается динамически аннотацией @EmbeddedKafka
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    private KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private Consumer<String, Object> testConsumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", "mock://test-scope");

        DefaultKafkaProducerFactory<String, GenericRecord> pf = new DefaultKafkaProducerFactory<>(producerProps);
        this.kafkaTemplate = new KafkaTemplate<>(pf);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://test-scope");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("specific.avro.reader", "true");

        var consumerFactory = new DefaultKafkaConsumerFactory<String, Object>(consumerProps);
        testConsumer = consumerFactory.createConsumer();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE inbox, outbox, accounts CASCADE");
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    @Nested
    class DebitTests {
        @Test
        @Tag("success funds debited pipeline logic")
        void shouldSuccessfullyDebitAccountWhenFundsAreSufficient() {
            testConsumer.subscribe(Collections.singletonList(DEBITED_TOPIC));
            UUID userId = UUID.randomUUID();
            Account account = Account.createAccount(userId, Currency.USD, new Notification());
            account.credit(Money.of(new BigDecimal("100.00"), Currency.USD), new Notification());
            jdbcTemplate.update("INSERT INTO accounts (id, user_id, balance, currency, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    account.getId(),
                    account.getUserId(),
                    account.getBalance().getAmount(),
                    account.getCurrency().toString(),
                    1,
                    account.getCreatedAt(),
                    account.getUpdatedAt()
            );

            DebitFunds debitEvent = DebitFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(account.getId())
                    .setAmount(new BigDecimal("30.00"))
                    .setCurrency("USD")
                    .setTimestamp(Instant.now())
                    .build();

            kafkaTemplate.send(DEBIT_TOPIC, debitEvent.getAccountId().toString(), debitEvent);

            Awaitility.await()
                    .atMost(80, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofMillis(5000))
                    .untilAsserted(() -> {
                        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(1));

                        assertThat(records.count())
                                .withFailMessage("Ожидалось хотя бы одно сообщение в топике, но ничего не пришло")
                                .isGreaterThanOrEqualTo(1);

                        Object receivedEvent = records.iterator().next().value();
                        assertThat(receivedEvent).isInstanceOf(FundsDebited.class);

                        FundsDebited fundsDebited = (FundsDebited) receivedEvent;
                        assertThat(fundsDebited.getAccountId()).isEqualTo(account.getId());

                        Account updatedAccount = accountRepository.findAccount(account.getId()).orElseThrow();
                        assertThat(updatedAccount.getBalance().getAmount()).isEqualByComparingTo("70.00");
                    });
        }


        @Test
        @Tag("failure funds debited pipeline logic")
        void shouldPublishDebitFailedEventWhenAccountNotFound() {
            testConsumer.subscribe(Collections.singletonList(DEBIT_FAILED_TOPIC));
            UUID nonExistentAccountId = UUID.randomUUID();

            DebitFunds debitEvent = DebitFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(nonExistentAccountId)
                    .setAmount(new BigDecimal("50.00"))
                    .setCurrency("USD")
                    .setTimestamp(Instant.now())
                    .build();

            kafkaTemplate.send(DEBIT_TOPIC, debitEvent.getAccountId().toString(), debitEvent);

            Awaitility.await().atMost(80, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));
                assertThat(records.count()).isGreaterThanOrEqualTo(1);
                Object receivedEvent = records.iterator().next().value();
                assertThat(receivedEvent).isInstanceOf(DebitFailed.class);
                DebitFailed debitFailed = (DebitFailed) receivedEvent;
                assertThat(debitFailed.getAccountId()).isEqualTo(nonExistentAccountId);
            });
        }

        @Test
        @Tag("failure funds debited pipeline logic")
        void shouldPublishDebitFailedEventWhenFundsAreInsufficient() {
            testConsumer.subscribe(Collections.singletonList(DEBIT_FAILED_TOPIC));
            UUID userId = UUID.randomUUID();
            Account account = Account.createAccount(userId, Currency.USD, new Notification());
            account.credit(Money.of(new BigDecimal("20.00"), Currency.USD), new Notification());
            jdbcTemplate.update("INSERT INTO accounts (id, user_id, balance, currency, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    account.getId(),
                    account.getUserId(),
                    account.getBalance().getAmount(),
                    account.getCurrency().toString(),
                    1,
                    account.getCreatedAt(),
                    account.getUpdatedAt()
            );

            DebitFunds debitEvent = DebitFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(account.getId())
                    .setAmount(new BigDecimal("50.00"))
                    .setCurrency("USD")
                    .setTimestamp(Instant.now())
                    .build();

            kafkaTemplate.send(DEBIT_TOPIC, debitEvent.getAccountId().toString(), debitEvent);

            Awaitility.await().atMost(80, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));
                assertThat(records.count()).isGreaterThanOrEqualTo(1);
                Object receivedEvent = records.iterator().next().value();
                assertThat(receivedEvent).isInstanceOf(DebitFailed.class);
                DebitFailed debitFailed = (DebitFailed) receivedEvent;
                assertThat(debitFailed.getAccountId()).isEqualTo(account.getId());
            });
        }
    }


    @Nested
    class CreditTests {

        @Test
        @Tag("success funds credited pipeline logic")
        public void shouldSuccessfullyCreditAccount() {
            testConsumer.subscribe(Collections.singletonList(CREDITED_TOPIC));
            UUID userId = UUID.randomUUID();
            Account account = Account.createAccount(userId, Currency.USD, new Notification());
            account.credit(Money.of(BigDecimal.ZERO, Currency.USD), new Notification());
            jdbcTemplate.update("INSERT INTO accounts (id, user_id, balance, currency, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    account.getId(),
                    account.getUserId(),
                    account.getBalance().getAmount(),
                    account.getCurrency().toString(),
                    1,
                    account.getCreatedAt(),
                    account.getUpdatedAt()
            );


            CreditFunds creditEvent = CreditFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(account.getId())
                    .setAmount(new BigDecimal("50.00"))
                    .setCurrency("USD")
                    .setTimestamp(Instant.now())
                    .build();


            kafkaTemplate.send(CREDIT_TOPIC, creditEvent.getAccountId().toString(), creditEvent);


            Awaitility.await()
                    .atMost(80, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofMillis(5000))
                    .untilAsserted(() -> {

                        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(1));
                        assertThat(records.count())
                                .withFailMessage("Ожидалось хотя бы одно сообщение в топике, но ничего не пришло")
                                .isGreaterThanOrEqualTo(1);

                        Object receivedEvent = records.iterator().next().value();
                        assertThat(receivedEvent).isInstanceOf(FundsCredited.class);

                        FundsCredited fundsCredited = (FundsCredited) receivedEvent;
                        assertThat(fundsCredited.getAccountId()).isEqualTo(account.getId());

                        Account updatedAccount = accountRepository.findAccount(account.getId()).orElseThrow();
                        assertThat(updatedAccount.getBalance().getAmount()).isEqualByComparingTo("50.00");
                    });
        }

        @Test
        @Tag("failure funds credited pipeline logic")
        void shouldPublishCreditFailedEventWhenAccountNotFound() {
            testConsumer.subscribe(Collections.singletonList(CREDIT_FAILED_TOPIC));
            UUID nonExistentAccountId = UUID.randomUUID();

            CreditFunds creditEvent = CreditFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(nonExistentAccountId)
                    .setAmount(new BigDecimal("50.00"))
                    .setCurrency("USD")
                    .setTimestamp(Instant.now())
                    .build();

            kafkaTemplate.send(CREDIT_TOPIC, creditEvent.getAccountId().toString(), creditEvent);

            Awaitility.await().atMost(80, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));
                assertThat(records.count()).isGreaterThanOrEqualTo(1);
                Object receivedEvent = records.iterator().next().value();
                assertThat(receivedEvent).isInstanceOf(CreditFailed.class);
                CreditFailed creditFailed = (CreditFailed) receivedEvent;
                assertThat(creditFailed.getAccountId()).isEqualTo(nonExistentAccountId);
            });
        }

        @Test
        @Tag("failure funds credited pipeline logic")
        void shouldPublishCreditFailedEventWhenCurrencyIsDifferent() {
            testConsumer.subscribe(Collections.singletonList(CREDIT_FAILED_TOPIC));
            UUID userId = UUID.randomUUID();
            Account account = Account.createAccount(userId, Currency.USD, new Notification());
            account.credit(Money.of(new BigDecimal("20.00"), Currency.USD), new Notification());
            jdbcTemplate.update("INSERT INTO accounts (id, user_id, balance, currency, version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    account.getId(),
                    account.getUserId(),
                    account.getBalance().getAmount(),
                    account.getCurrency().toString(),
                    1,
                    account.getCreatedAt(),
                    account.getUpdatedAt()
            );

            CreditFunds creditFunds = CreditFunds.newBuilder()
                    .setCommandId(UUID.randomUUID())
                    .setCorrelationId(UUID.randomUUID())
                    .setAccountId(account.getId())
                    .setAmount(new BigDecimal("50.00"))
                    .setCurrency("EUR")
                    .setTimestamp(Instant.now())
                    .build();

            kafkaTemplate.send(CREDIT_TOPIC, creditFunds.getAccountId().toString(), creditFunds);

            Awaitility.await().atMost(80, TimeUnit.SECONDS).untilAsserted(() -> {
                ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));
                assertThat(records.count()).isGreaterThanOrEqualTo(1);
                Object receivedEvent = records.iterator().next().value();
                assertThat(receivedEvent).isInstanceOf(CreditFailed.class);
                CreditFailed creditFailed = (CreditFailed) receivedEvent;
                assertThat(creditFailed.getAccountId()).isEqualTo(account.getId());
            });
        }
    }
}
