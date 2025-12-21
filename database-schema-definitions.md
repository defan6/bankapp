# Спецификация Схем Баз Данных "Core Banking System 2.0"

**Версия:** 1.0
**Дата:** 21.12.2025

Этот документ описывает структуру таблиц для баз данных каждого микросервиса. Для сервисов, использующих реляционную СУБД, выбран PostgreSQL. Для аналитического сервиса — ClickHouse.

---

### 1. User Service (PostgreSQL)

Сервис управляет пользователями и их аутентификационными данными.

#### Таблица `users`

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Уникальный идентификатор пользователя |
| `email` | `VARCHAR(255)` | `NOT NULL, UNIQUE` | Адрес электронной почты (логин) |
| `password_hash`| `VARCHAR(255)`| `NOT NULL` | Хеш пароля |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Время создания записи |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Время последнего обновления |

---

### 2. Account Service (PostgreSQL)

Сервис управляет счетами пользователей.

#### Таблица `accounts`

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Уникальный идентификатор счета |
| `user_id` | `UUID` | `NOT NULL, INDEX` | ID владельца счета (связь с User Service) |
| `balance` | `DECIMAL(19, 4)` | `NOT NULL` | Баланс счета |
| `currency` | `VARCHAR(3)` | `NOT NULL` | Трехбуквенный код валюты (RUB, USD) |
| `version` | `BIGINT` | `NOT NULL` | Версия для оптимистичной блокировки |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Время создания записи |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Время последнего обновления |

---

### 3. Transaction Service (PostgreSQL)

Сервис координирует транзакции и реализует паттерн Transactional Outbox.

#### Таблица `transactions`

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Уникальный идентификатор транзакции Saga |
| `source_account_id` | `UUID` | `NOT NULL, INDEX` | Счет списания |
| `destination_account_id`| `UUID` | `NOT NULL, INDEX` | Счет зачисления |
| `amount` | `DECIMAL(19, 4)` | `NOT NULL` | Сумма транзакции |
| `currency` | `VARCHAR(3)` | `NOT NULL` | Валюта транзакции |
| `status` | `VARCHAR(50)` | `NOT NULL` | Статус саги (PENDING, COMPLETED, FAILED) |
| `failure_reason`| `TEXT` | | Причина сбоя, если он произошел |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Время создания записи |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Время последнего обновления |

#### Таблица `outbox`

Эта таблица — ядро паттерна Transactional Outbox.

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Уникальный идентификатор события |
| `aggregate_type` | `VARCHAR(255)`| `NOT NULL` | Тип агрегата (например, 'transaction') |
| `aggregate_id` | `VARCHAR(255)`| `NOT NULL` | ID агрегата (например, ID транзакции) |
| `topic` | `VARCHAR(255)`| `NOT NULL` | Топик в Kafka для отправки |
| `payload` | `JSONB` | `NOT NULL` | Тело сообщения для отправки в Kafka |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Время создания события |

---

### 4. History Service (PostgreSQL)

Read-модель для быстрого получения истории транзакций. Данные денормализованы.

#### Таблица `transaction_history`

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | ID транзакции (такой же, как в Transaction Service) |
| `user_id` | `UUID` | `NOT NULL, INDEX` | ID пользователя, для которого эта запись в истории |
| `type` | `VARCHAR(50)` | `NOT NULL` | Тип операции для пользователя (INCOMING, OUTGOING) |
| `source_account_id` | `UUID` | | Счет списания |
| `destination_account_id`| `UUID` | | Счет зачисления |
| `amount` | `DECIMAL(19, 4)` | `NOT NULL` | Сумма транзакции |
| `currency` | `VARCHAR(3)` | `NOT NULL` | Валюта |
| `status` | `VARCHAR(50)` | `NOT NULL` | Финальный статус (COMPLETED, FAILED) |
| `event_timestamp`| `TIMESTAMPTZ` | `NOT NULL` | Время завершения транзакции |

---

### 5. Scheduler Service (PostgreSQL)

Хранит информацию о запланированных/регулярных платежах.

#### Таблица `payment_schedules`

| Колонка | Тип | Ограничения | Описание |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` | `PRIMARY KEY` | Уникальный идентификатор расписания |
| `user_id` | `UUID` | `NOT NULL, INDEX` | ID пользователя-владельца |
| `source_account_id` | `UUID` | `NOT NULL` | Счет списания |
| `destination_account_id`| `UUID` | `NOT NULL` | Счет зачисления |
| `amount` | `DECIMAL(19, 4)` | `NOT NULL` | Сумма |
| `currency` | `VARCHAR(3)` | `NOT NULL` | Валюта |
| `cron_expression`| `VARCHAR(255)`| `NOT NULL` | CRON-выражение для расписания |
| `is_active` | `BOOLEAN` | `NOT NULL, DEFAULT true` | Флаг активности расписания |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL` | Время создания записи |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL` | Время последнего обновления |

---

### 6. Analytics Service (ClickHouse)

Хранилище для аналитических запросов. Используется одна широкая денормализованная таблица.

#### Таблица `transactions_log` (движок `MergeTree`)

| Колонка | Тип | Описание |
| :--- | :--- | :--- |
| `transaction_id` | `UUID` | ID транзакции |
| `source_account_id`| `UUID` | Счет списания |
| `destination_account_id`| `UUID` | Счет зачисления |
| `amount` | `Decimal(19, 4)` | Сумма |
| `currency` | `String` | Валюта |
| `status` | `String` | Финальный статус транзакции |
| `event_timestamp`| `DateTime` | Время события |
| `processing_timestamp`| `DateTime` | Время загрузки записи в ClickHouse |
