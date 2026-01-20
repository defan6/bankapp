# Сценарии взаимодействия с Account Service

Этот документ описывает ключевые сценарии взаимодействия с `Account Service`, разделяя их на синхронные (API) и
асинхронные (на основе событий Kafka).
 
---

### Сценарий 1: Создание нового счета (Синхронный)

**Цель:** Пользователь открывает новый банковский счет.

**Действующие лица:** Пользователь, API Gateway, Account Service, База данных.

**Последовательность:**

1. **Пользователь** отправляет `POST` запрос на `/api/accounts` с указанием валюты (например, `{"currency": "RUB"}`).
2. **API Gateway** маршрутизирует запрос в `Account Service`.
3. **`AccountController`** принимает запрос.
4. **`AccountController`** вызывает `AccountUseCase.createAccount(createAccountCommand)`, передавая данные из запроса.
5. **`AccountServiceImpl`** (реализация UseCase):
    * Создает новую доменную сущность `Account` с уникальным `accountId` и начальным балансом 0.
    * Извлекает `userId` из токена аутентификации.
    * Вызывает `AccountRepository.save(newAccount)`.
6. **`AccountPersistenceAdapter`** (реализация Repository):
    * Преобразует доменную модель `Account` в JPA-сущность `AccountDbo`.
    * Сохраняет новую запись в таблицу `accounts` в базе данных.
7. **`AccountController`** получает ответ об успешном создании, преобразует его в DTO и возвращает клиенту ответ
   `201 Created` с данными нового счета.

---

### Сценарий 2: Списание средств (Асинхронный)

**Цель:** `Transaction Service` в рамках бизнес-процесса перевода денег дает команду `Account Service` списать средства
со счета. Здесь раскрываются все паттерны надежности (Idempotent Consumer, Transactional Outbox, Optimistic Locking).

**Действующие лица:** Transaction Service, Kafka, Account Service, База данных.

**Последовательность:**

1. **`Transaction Service`** начинает сагу перевода и отправляет в топик Kafka команду `DebitFundsCommand`. Эта команда
   содержит:
    * `commandId` (уникальный ID для идемпотентности)
    * `accountId` (с какого счета списать)
    * `amount` (сумма)
2. **`AccountCommandConsumer`** в `Account Service` получает это сообщение.
3. **НАЧАЛО ТРАНЗАКЦИИ В БД.**
4. **Проверка на идемпотентность:** `AccountServiceImpl` пытается вставить `commandId` в таблицу `inbox`.
    * **Если вставка успешна:** Команда новая, продолжаем.
    * **Если ошибка (дубликат):** Команда уже обрабатывалась. Транзакция завершается, сообщение игнорируется. **Конец
      сценария.**
5. **Бизнес-логика:** `AccountServiceImpl` вызывает `AccountRepository.findById(accountId)`.
6. **`AccountPersistenceAdapter`** загружает сущность `AccountDbo` из БД, используя **оптимистическую блокировку** (поле
   `version`).
7. **`AccountServiceImpl`** выполняет доменную логику:
    * Проверяет, достаточен ли баланс (`account.balance >= debit.amount`).
    * **Если нет:** Формируется событие `DebitFailed`.
    * **Если да:** Вычисляется новый баланс. Формируется событие `FundsDebited`.
8. **Сохранение результата:** `AccountServiceImpl` вызывает `AccountRepository.save(account)`.
   `AccountPersistenceAdapter` отправляет `UPDATE` в БД, который включает `WHERE version = 'старое_значение'`.
    * **Если `UPDATE` не прошел (0 строк обновлено):** Значит, другой процесс изменил счет. Выбрасывается
      `OptimisticLockException`, транзакция откатывается, и обработка команды может быть повторена.
9. **Паттерн Transactional Outbox:** `AccountServiceImpl` создает доменное событие (например, `FundsDebited`) и вызывает
   `EventPublisher.publish(event)`.
10. **`OutboxPersistenceAdapter`** (реализация Publisher) вставляет запись о событии (`FundsDebited` или `DebitFailed`)
    в таблицу `outbox`.
11. **КОНЕЦ ТРАНЗАКЦИИ В БД.** Все изменения (`inbox`, `accounts`, `outbox`) атомарно сохраняются.
12. **Асинхронная отправка:**
    * Отдельный процесс **`MessageRelay`** периодически опрашивает таблицу `outbox`.
    * Находит новую запись.
    * Отправляет событие из `payload` в соответствующий топик Kafka.
    * После подтверждения от Kafka, `MessageRelay` удаляет запись из `outbox`.
13. **`Transaction Service`** (или другие сервисы) получает событие `FundsDebited` и продолжает свою сагу.

---

### Сценарий 3: Зачисление средств (Асинхронный)

**Цель:** `Transaction Service` дает команду `Account Service` зачислить средства на счет.

**Действующие лица:** Transaction Service, Kafka, Account Service, База данных.

**Последовательность:**

1. **`Transaction Service`** шлет команду `CreditFundsCommand` в топик Kafka (содержит `commandId`, `accountId`,
   `amount`).
2. **`AccountCommandConsumer`** получает это сообщение.
3. **НАЧАЛО ТРАНЗАКЦИИ В БД.**
4. **Проверка на идемпотентность:** Аналогично сценарию списания, проверяется `commandId` в таблице `inbox`.
5. **Бизнес-логика:** `AccountServiceImpl` загружает счет `Account` из БД (с `version`).
6. **`AccountServiceImpl`** выполняет доменную логику:
    * Увеличивает баланс на указанную сумму.
    * Формирует событие `FundsCredited`.
7. **Сохранение результата:** `AccountServiceImpl` вызывает `AccountRepository.save(account)`, обновляя запись в БД с
   инкрементом `version`.
8. **Паттерн Transactional Outbox:** `AccountServiceImpl` создает доменное событие (`FundsCredited`) и вызывает
   `EventPublisher.publish(event)`.
9. **`OutboxPersistenceAdapter`** вставляет запись о событии `FundsCredited` в таблицу `outbox`.
10. **КОНЕЦ ТРАНЗАКЦИИ В БД.** Все изменения (`inbox`, `accounts`, `outbox`) атомарно сохраняются.
11. **Асинхронная отправка:** Аналогично сценарию списания, `MessageRelay` отправляет событие `FundsCredited` в Kafka и
    удаляет его из `outbox`.
12. **`Transaction Service`** (или другие сервисы) получает событие `FundsCredited` и продолжает свою сагу.
