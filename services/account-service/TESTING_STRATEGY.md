# Стратегия тестирования для account-service

Этот документ описывает пошаговый план тестирования для сервиса `account-service`, основанный на методологии "Пирамиды тестирования" и принципах гексагональной архитектуры.

## 1. Unit-тесты (Модульное тестирование)

**Цель:** Проверить правильность бизнес-логики ядра приложения в полной изоляции от инфраструктуры (базы данных, брокеров сообщений и т.д.). Эти тесты должны быть максимально быстрыми.

- **Расположение:** `src/test/java/`
- **Инструменты:** JUnit 5, Mockito

### Шаги:

1.  **Тестирование Use Cases / Services:**
    -   **Кандидат:** `com.bankapp.accountservice.application.service.DefaultAccountService`.
    -   Создать тестовый класс `DefaultAccountServiceTest`.
    -   В каждом тесте **мокировать** все зависимости (исходящие порты), такие как `AccountRepository`, `DebitCommandPublisher`, и т.д.
    -   Написать тесты для каждого бизнес-сценария:
        -   Успешное списание средств.
        -   Попытка списания при недостаточном балансе.
        -   Попытка списания в другой валюте.
        -   Успешное пополнение счета.
        -   И так далее.

## 2. Интеграционные тесты

**Цель:** Проверить корректность работы адаптеров и их взаимодействие с ядром приложения и внешней инфраструктурой.

- **Расположение:** `src/integrationTest/java/`
- **Инструменты:** JUnit 5, Spring Boot Test, Testcontainers, MockMvc, EmbeddedKafka.

### 2.1. Тестирование слоя персистенции (Persistence Adapters)

- **Кандидат:** `com.bankapp.accountservice.adapters.out.jpa.AccountJpaAdapter`.
- **Инструменты:** `@DataJpaTest`, Testcontainers (PostgreSQL).
- **Шаги:**
    1.  Создать тестовый класс `AccountJpaAdapterTest`.
    2.  Написать тесты, которые вызывают методы адаптера (`save`, `findAccount`, `findAllAccountsByUserId`).
    3.  Проверять, что данные корректно сохраняются, извлекаются и маппятся между доменной моделью и JPA-сущностью.

### 2.2. Тестирование входящих сообщений (Message Adapters)

- **Кандидат:** `com.bankapp.accountservice.application.consumers.AccountCommandConsumer`.
- **Инструменты:** `@SpringBootTest`, `@EmbeddedKafka`, Testcontainers.
- **Шаги:**
    1.  Тестовый класс `AccountCommandConsumerTest` уже существует, его нужно проверить и доработать.
    2.  В тестах отправлять сообщения (`DebitFunds`, `CreditFunds`) в тестовый топик Kafka.
    3.  Проверять, что консьюмер корректно обрабатывает сообщение: изменяет баланс в базе данных и отправляет ответное событие (`FundsDebited`, `DebitFailed`).

### 2.3. Тестирование API (Web Adapters)

- **Кандидат:** `com.bankapp.accountservice.adapters.in.web.AccountControllerV1`.
- **Инструменты:** `@SpringBootTest`, `MockMvc`, Testcontainers.
- **Шаги:**
    1.  Создать тестовый класс `AccountControllerV1Test`.
    2.  Для каждого эндпоинта написать тесты, проверяющие всю цепочку (Happy Path, ошибки валидации, 404 Not Found и т.д.).
        - `GET /api/v1/accounts/{accountId}`
        - `GET /api/v1/accounts?userId={userId}`
        - `POST /api/v1/accounts`

## 3. Порядок действий

Предлагаю двигаться от центра гексагона наружу:

1.  **Начать с Unit-тестов:** Написать тесты для `DefaultAccountService`. Это даст уверенность в том, что бизнес-логика работает.
2.  **Перейти к интеграционным тестам адаптеров:**
    -   Написать тесты для `AccountJpaAdapter`.
    -   Написать/дополнить тесты для `AccountControllerV1`.
    -   Проверить/дополнить тесты для `AccountCommandConsumer`.

Этот подход позволит нам планомерно и надежно покрыть весь функционал сервиса тестами.
