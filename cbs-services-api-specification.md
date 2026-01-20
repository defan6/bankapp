# Спецификация Сервисов и API "Core Banking System 2.0"

**Версия:** 1.0
**Дата:** 21.12.2025

Этот документ описывает архитектуру, зоны ответственности, API эндпоинты и асинхронные взаимодействия для каждого микросервиса в проекте.

---

### 1. API Gateway

**Назначение:** Единая точка входа, проксирующая запросы к внутренним сервисам. Выполняет централизованную аутентификацию, применяет политики безопасности (rate limiting) и обеспечивает маршрутизацию.

**Технологии:** Nginx / Spring Cloud Gateway / Ocelot

**Эндпоинты:** Не имеет собственных бизнес-эндпоинтов. Все эндпоинты, описанные ниже, доступны через Gateway.

**Правила маршрутизации:**
- `/api/users/**` -> **User Service**
- `/api/accounts/**` -> **Account Service**
- `/api/transactions/**` -> **Transaction Service**
- `/api/history/**` -> **History Service**
- `/api/schedules/**` -> **Scheduler Service**

---

### 2. User Service (Сервис Пользователей)

**Назначение:** Управляет жизненным циклом пользователя: регистрация, вход, управление профилем. Является источником правды для идентификации пользователя в системе.

**База данных:** PostgreSQL

**Асинхронные взаимодействия:**
- **Публикует в Kafka:**
  - `users.registered` (событие): Информирует систему о регистрации нового пользователя.
    - `Payload: { "userId": "uuid", "email": "user@example.com", "timestamp": "iso_datetime" }`

**API Эндпоинты:**

- `POST /api/users/register`
  - **Описание:** Регистрация нового пользователя.
  - **Request Body:** `{ "email": "user@example.com", "password": "strongpassword123" }`
  - **Success Response (201 Created):** `{ "userId": "uuid", "email": "user@example.com" }`
  - **Error Responses:** `400 Bad Request` (невалидные данные), `409 Conflict` (email уже занят).

- `POST /api/users/login`
  - **Описание:** Аутентификация пользователя и выдача JWT.
  - **Request Body:** `{ "email": "user@example.com", "password": "strongpassword123" }`
  - **Success Response (200 OK):** `{ "token": "ey...", "tokenType": "Bearer" }`
  - **Error Responses:** `401 Unauthorized`.

- `GET /api/users/me`
  - **Описание:** Получение информации о текущем аутентифицированном пользователе.
  - **Headers:** `Authorization: Bearer <token>`
  - **Success Response (200 OK):** `{ "userId": "uuid", "email": "user@example.com" }`
  - **Error Responses:** `401 Unauthorized`.

---

### 3. Account Service (Сервис Счетов)

**Назначение:** Управляет банковскими счетами пользователей: создание, получение информации, обновление баланса.

**База данных:** PostgreSQL

**Асинхронные взаимодействия:**
- **Подписывается на Kafka (в рамках Saga):**
  - `transaction.funds.debit.request`: Команда на списание средств.
  - `transaction.funds.credit.request`: Команда на зачисление средств.
- **Публикует в Kafka (в рамках Saga):**
  - `account.funds.debited`: Событие об успешном списании.
  - `account.funds.credited`: Событие об успешном зачислении.
  - `account.debit.failed`: Событие о неудачном списании (например, недостаточно средств).
  - `account.credit.failed`: Событие о неудачном зачислении.

**API Эндпоинты:**

- `POST /api/accounts`
  - **Описание:** Создание нового счета для текущего пользователя.
  - **Headers:** `Authorization: Bearer <token>`
  - **Request Body:** `{ "currency": "RUB" }`
  - **Success Response (201 Created):** `{ "accountId": "uuid", "userId": "uuid", "balance": 0.0, "currency": "RUB" }`
  - **Error Responses:** `401 Unauthorized`, `400 Bad Request`.

- `GET /api/accounts`
  - **Описание:** Получение списка всех счетов текущего пользователя.
  - **Headers:** `Authorization: Bearer <token>`
  - **Success Response (200 OK):** `[{ "accountId": "uuid", "balance": 1500.50, "currency": "RUB" }]`
  - **Error Responses:** `401 Unauthorized`.
- `GET api/accounts/{id}`
- **Описание:** Получение аккаунта по id.
- **Headers:** `Authorization: Bearer <token>`
- **Success Response (200 OK):** `{ "accountId": "uuid", "balance": 1500.50, "currency": "RUB" }`
- **Error Responses:** `401 Unauthorized`, `403 Forbidden`.

---

### 4. Transaction Service (Сервис Транзакций)

**Назначение:** Оркестрация распределенной транзакции по переводу средств с использованием паттернов Saga и Transactional Outbox.

**База данных:** PostgreSQL (с таблицей `outbox`)

**Асинхронные взаимодействия:**
- **Основной координатор Saga:**
  - **Публикует:** `transaction.funds.debit.request`, `transaction.funds.credit.request`.
  - **Подписывается:** `account.funds.debited`, `account.funds.credited`, `account.debit.failed`, `account.credit.failed`.
  - **Публикует финальные статусы:** `transaction.completed`, `transaction.failed`.

**API Эндпоинты:**

- `POST /api/transactions/transfer`
  - **Описание:** Инициация перевода средств. Процесс выполняется асинхронно.
  - **Headers:** `Authorization: Bearer <token>`
  - **Request Body:** `{ "fromAccountId": "uuid", "toAccountId": "uuid", "amount": 100.00 }`
  - **Success Response (202 Accepted):** `{ "transactionId": "uuid", "status": "PENDING" }`
  - **Error Responses:** `401 Unauthorized`, `403 Forbidden` (не владелец счета списания), `400 Bad Request`.

- `GET /api/transactions/{transactionId}`
  - **Описание:** Получение статуса конкретной транзакции.
  - **Headers:** `Authorization: Bearer <token>`
  - **Success Response (200 OK):** `{ "transactionId": "uuid", "status": "COMPLETED|PENDING|FAILED", "amount": 100.00, ... }`
  - **Error Responses:** `401 Unauthorized`, `404 Not Found`.

---

### 5. History Service (Сервис Истории)

**Назначение:** Предоставляет оптимизированный для чтения доступ к истории транзакций пользователя (CQRS Read Model).

**База данных:** PostgreSQL (денормализованная структура)

**Асинхронные взаимодействия:**
- **Подписывается на Kafka:**
  - `transaction.completed`: Для сохранения успешной операции.
  - `transaction.failed`: Для сохранения неудавшейся операции.

**API Эндпоинты:**

- `GET /api/history`
  - **Описание:** Получение истории операций для текущего пользователя с пагинацией.
  - **Headers:** `Authorization: Bearer <token>`
  - **Query Params:** `?page=0&size=20&accountId=uuid`
  - **Success Response (200 OK):** `{ "content": [ ... ], "page": 0, "size": 20, "totalPages": 5 }`
  - **Error Responses:** `401 Unauthorized`.

---

### 6. Notification Service (Сервис Уведомлений)

**Назначение:** Отправляет пользователям уведомления (email/push) об их действиях в системе.

**База данных:** Нет (или Redis для контроля частоты отправок).

**Асинхронные взаимодействия:**
- **Подписывается на Kafka:**
  - `users.registered`
  - `transaction.completed`
  - `transaction.failed`

**API Эндпоинты:** Нет публичных эндпоинтов.

---

### 7. Analytics Service (Сервис Аналитики)

**Назначение:** Потребляет поток событий из Kafka и загружает их в аналитическое хранилище для дальнейшего анализа.

**База данных:** ClickHouse

**Асинхронные взаимодействия:**
- **Подписывается на Kafka:**
  - `transaction.completed` и другие бизнес-события для сбора метрик (объем транзакций, средний чек и т.д.).

**API Эндпоинты:** Нет публичных эндпоинтов. Может иметь внутренние эндпоинты для Grafana.

---

### 8. Scheduler Service (Сервис Запланированных Операций)

**Назначение:** Управляет созданием, выполнением и удалением регулярных/отложенных платежей.

**База данных:** PostgreSQL

**Асинхронные взаимодействия:**
- **Публикует в Kafka (или делает HTTP-запрос):**
  - Инициирует транзакцию, отправляя команду в **Transaction Service**.

**API Эндпоинты:**

- `POST /api/schedules`
  - **Описание:** Создание нового регулярного платежа.
  - **Headers:** `Authorization: Bearer <token>`
  - **Request Body:** `{ "fromAccountId": "uuid", "toAccountId": "uuid", "amount": 50.0, "cronExpression": "0 0 12 1 * ?" }`
  - **Success Response (201 Created):** `{ "scheduleId": "uuid", ... }`
  - **Error Responses:** `401 Unauthorized`, `400 Bad Request`.

- `GET /api/schedules`
  - **Описание:** Получение списка всех регулярных платежей пользователя.
  - **Headers:** `Authorization: Bearer <token>`
  - **Success Response (200 OK):** `[ { "scheduleId": "uuid", ... } ]`
  - **Error Responses:** `401 Unauthorized`.

- `DELETE /api/schedules/{scheduleId}`
  - **Описание:** Удаление регулярного платежа.
  - **Headers:** `Authorization: Bearer <token>`
  - **Success Response (204 No Content):**
  - **Error Responses:** `401 Unauthorized`, `403 Forbidden`, `404 Not Found`.

---
### 9. Контракты Конфигурации (Переменные Окружения)

Этот раздел описывает переменные окружения, необходимые для запуска и конфигурации сервисов.

#### Общие для всех сервисов
- `LOG_LEVEL`: Уровень логирования (`INFO`, `DEBUG`, `WARN`, `ERROR`).
- `KAFKA_BOOTSTRAP_SERVERS`: Адреса брокеров Kafka (например, `kafka:9092`).
- `OTEL_EXPORTER_OTLP_ENDPOINT`: URL коллектора OpenTelemetry (например, `http://jaeger:4317`).

#### Специфичные для сервисов
- `DATABASE_URL`: Строка подключения к PostgreSQL. **Требуется для:** `User Service`, `Account Service`, `Transaction Service`, `History Service`, `Scheduler Service`.
- `CLICKHOUSE_URL`: Строка подключения к ClickHouse. **Требуется для:** `Analytics Service`.
- `REDIS_URL`: Строка подключения к Redis. **Требуется для:** `API Gateway` (для rate limiting), `Notification Service` (для контроля частоты).
- `JWT_SECRET`: Секретный ключ для подписи и валидации JWT токенов. **Требуется для:** `User Service` (подпись), `API Gateway` (валидация).

---
### 10. Контракты Observability (Наблюдаемости)

Этот раздел определяет стандарты для сбора телеметрии, чтобы обеспечить прозрачность и простоту отладки системы.

#### Логирование
- **Формат:** Все сервисы **обязаны** писать логи в `stdout` в виде **структурированного JSON**.
- **Обязательные поля:** Каждая запись лога **должна** содержать минимальный набор полей для эффективной агрегации и поиска:
  - `timestamp`: Время события в формате ISO 8601.
  - `level`: Уровень лога (`info`, `error`, и т.д.).
  - `service.name`: Имя сервиса (например, `user-service`).
  - `message`: Текст лога.
  - `trace.id`: ID сквозной трассировки (если есть).
  - `span.id`: ID текущего спана трассировки (если есть).

#### Метрики
- **Формат:** Все сервисы **обязаны** предоставлять метрики в формате, совместимом с Prometheus.
- **Эндпоинт:** Метрики **должны** быть доступны по стандартному пути `/metrics`.
- **Обязательные метрики:** Каждый сервис должен собирать как минимум:
  - Метрики по HTTP-запросам (количество, длительность, коды ответов).
  - Метрики по работе с Kafka (lag консьюмеров, количество отправленных/принятых сообщений).
  - Метрики по работе с базой данных (время выполнения запросов).

#### Трейсинг
- **Стандарт:** Все сервисы **обязаны** поддерживать стандарт **W3C Trace Context** для распространения контекста трассировки.
- **Инструментация:** Рекомендуется использовать автоматическую инструментацию через SDK **OpenTelemetry** для соответствующего языка/фреймворка. Это обеспечивает автоматическое создание спанов для HTTP-запросов, вызовов БД и сообщений Kafka.