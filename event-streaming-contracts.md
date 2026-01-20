# Контракты Потоковой Передачи Событий (Kafka)

**Версия:** 1.0
**Дата:** 21.12.2025

Этот документ является источником правды для всех событий и команд, передаваемых через Apache Kafka. Он определяет их
структуру, топики, производителей и потребителей.

### Общие принципы

1. **Версионирование:** Каждое событие имеет версию (например, `UserRegistered.v1`), чтобы обеспечить обратную
   совместимость при изменениях. Тип события передается в заголовках сообщения Kafka или в самом payload.
2. **Идемпотентность:** Каждое событие содержит `eventId` (или `commandId`). Потребители **обязаны** использовать это
   поле для предотвращения повторной обработки одного и того же сообщения.
3. **Корреляция:** В рамках одного бизнес-процесса (например, Saga-транзакции) все события и команды должны содержать
   общий `correlationId` (в нашем случае это `transactionId`) для легкой трассировки и отладки.

---

## 1. События Пользователей

#### **Событие: `UserRegistered.v1`**

- **Топик:** `user-events`
- **Производитель:** `User Service`
- **Потребители:** `Notification Service`
- **Описание:** Публикуется после успешной регистрации нового пользователя.
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "userId": "uuid",
    "email": "user@example.com",
    "timestamp": "iso_datetime"
  }
  ```

---

## 2. Команды и События в рамках Саги Перевода

Это ядро системы. Мы разделяем сообщения на **Команды** (приказ что-то сделать) и **События** (уведомление о том, что
что-то произошло).

### 2.1. Команды для `Account Service`

- **Топик для всех команд:** `account-commands`

#### **Команда: `DebitFunds.v1`**

- **Производитель:** `Transaction Service`
- **Потребитель:** `Account Service`
- **Описание:** Приказ списать средства со счета.
- **Схема Payload:**
  ```json
  {
    "commandId": "uuid",
    "correlationId": "uuid", 
    "accountId": "uuid",
    "amount": "100.50",
    "currency": "RUB",
    "timestamp": "iso_datetime"
  }
  ```

#### **Команда: `CreditFunds.v1`**

- **Производитель:** `Transaction Service`
- **Потребитель:** `Account Service`
- **Описание:** Приказ зачислить средства на счет.
- **Схема Payload:**
  ```json
  {
    "commandId": "uuid",
    "correlationId": "uuid", 
    "accountId": "uuid",
    "amount": "100.50",
    "currency": "RUB",
    "timestamp": "iso_datetime"
  }
  ```

#### **Команда: `RefundDebit.v1` (Компенсирующая)**

- **Производитель:** `Transaction Service`
- **Потребитель:** `Account Service`
- **Описание:** Компенсирующая команда. Приказ вернуть ранее списанные средства на счет.
- **Схема Payload:**
  ```json
  {
    "commandId": "uuid",
    "correlationId": "uuid", 
    "accountId": "uuid",
    "amount": "100.50",
    "currency": "RUB",
    "timestamp": "iso_datetime"
  }
  ```

### 2.2. События от `Account Service`

- **Топик для всех событий:** `account-events`

#### **Событие: `FundsDebited.v1`**

- **Производитель:** `Account Service`
- **Потребитель:** `Transaction Service`
- **Описание:** Уведомление об успешном списании средств.
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "correlationId": "uuid", 
    "accountId": "uuid",
    "timestamp": "iso_datetime"
  }
  ```

#### **Событие: `DebitFailed.v1`**

- **Производитель:** `Account Service`
- **Потребитель:** `Transaction Service`
- **Описание:** Уведомление о провале списания (например, недостаточно средств).
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "correlationId": "uuid", 
    "accountId": "uuid",
    "reason": "INSUFFICIENT_FUNDS",
    "timestamp": "iso_datetime"
  }
  ```

#### **Событие: `FundsCredited.v1`**

- **Производитель:** `Account Service`
- **Потребитель:** `Transaction Service`
- **Описание:** Уведомление об успешном зачислении средств.
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "correlationId": "uuid",
    "accountId": "uuid",
    "timestamp": "iso_datetime"
  }
  ```

#### **Событие: `CreditFailed.v1`**

- **Производитель:** `Account Service`
- **Потребитель:** `Transaction Service`
- **Описание:** Уведомление о провале зачисления (например, счет заблокирован).
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "correlationId": "uuid",
    "accountId": "uuid",
    "reason": "ACCOUNT_CLOSED_OR_BLOCKED",
    "timestamp": "iso_datetime"
  }
  ```

---

## 3. Финальные События Жизненного Цикла Транзакции

- **Топик для всех событий:** `transaction-lifecycle-events`

#### **Событие: `TransactionCompleted.v1`**

- **Производитель:** `Transaction Service`
- **Потребители:** `History Service`, `Notification Service`, `Analytics Service`
- **Описание:** Финальное событие. Транзакция успешно завершена.
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "transactionId": "uuid",
    "sourceAccountId": "uuid",
    "destinationAccountId": "uuid",
    "amount": "100.50",
    "currency": "RUB",
    "timestamp": "iso_datetime"
  }
  ```

#### **Событие: `TransactionFailed.v1`**

- **Производитель:** `Transaction Service`
- **Потребители:** `History Service`, `Notification Service`, `Analytics Service`
- **Описание:** Финальное событие. Транзакция провалилась и не была завершена.
- **Схема Payload:**
  ```json
  {
    "eventId": "uuid",
    "transactionId": "uuid",
    "sourceAccountId": "uuid",
    "destinationAccountId": "uuid",
    "amount": "100.50",
    "reason": "string",
    "timestamp": "iso_datetime"
  }
  ```
