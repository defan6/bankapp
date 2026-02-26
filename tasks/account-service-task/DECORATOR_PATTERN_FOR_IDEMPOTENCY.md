# Паттерн "Декоратор" для обеспечения Идемпотентности

Этот документ описывает решение проблемы "жесткой связи" при реализации идемпотентности в сервисном слое.

### Проблема

Изначально предполагалось, что проверка на идемпотентность (работа с `InboxRepository`) будет находиться прямо внутри
`AccountServiceImpl`.

**Пример "плохого" подхода:**

```java
public class AccountServiceImpl implements UpdateAccountBalanceUseCase {
    
    private final InboxRepository inboxRepository;
    // ... другие репозитории

    @Override
    public void processDebitFunds(DebitAccountCommand command) {
        // Проверка на идемпотентность "зашита" в бизнес-логику
        if (inboxRepository.existsById(command.commandId())) {
            return;
        }
        inboxRepository.save(...);

        // ... основная бизнес-логика ...
    }
}
```

**Недостаток:** Это смешивает основную бизнес-логику (списание средств) с технической задачей обеспечения
идемпотентности. Если для какого-то другого `UseCase` идемпотентность будет не нужна, нам придется усложнять код `if`
-ами.

### Решение: Паттерн "Декоратор"

Мы разделяем эти две ответственности, используя паттерн "Декоратор".

#### Шаг 1: "Чистый" `AccountServiceImpl`

Основной сервис содержит **только бизнес-логику**. Он ничего не знает про `InboxRepository`.

```java
@Service
// Этот сервис НЕ помечается как @Primary
public class AccountServiceImpl implements UpdateAccountBalanceUseCase {
    
    private final AccountRepository accountRepository;
    private final DomainEventDispatcher eventDispatcher;

    @Override
    @Transactional
    public void processDebitFunds(DebitAccountCommand command) {
        // НИКАКИХ проверок на идемпотентность!
        // Только чистая бизнес-логика:
        Account account = accountRepository.findAccount(command.accountId());
        account.debit(...);
        accountRepository.save(account);
        eventDispatcher.dispatch(...);
    }
}
```

#### Шаг 2: Класс-декоратор `IdempotentUseCaseDecorator`

Мы создаем "обертку", которая тоже реализует `UpdateAccountBalanceUseCase`.

```java
@Component
@Primary // <<< Важно! Говорим Spring, что это основная реализация для инъекции
@RequiredArgsConstructor
public class IdempotentUseCaseDecorator implements UpdateAccountBalanceUseCase {

    private final UpdateAccountBalanceUseCase decoratedService; // <<< "Оборачиваемый" сервис. Инжектим по интерфейсу.
    private final InboxRepository inboxRepository;

    @Override
    @Transactional
    public void processDebitFunds(DebitAccountCommand command) {
        
        // 1. СНАЧАЛА - логика декоратора (проверка идемпотентности)
        if (inboxRepository.existsById(command.commandId())) {
            log.warn("Command {} already processed, skipping.", command.commandId());
            return; // Прерываем выполнение, если команда - дубликат
        }
        inboxRepository.save(command.commandId(), ...); // Нужно доработать, чтобы сохранять и timestamp

        // 2. ПОТОМ - делегирование вызова "настоящему" сервису
        decoratedService.processDebitFunds(command);
    }
}
```

### Как это работает?

1. `AccountCommandConsumer` запрашивает у Spring бин типа `UpdateAccountBalanceUseCase`.
2. Благодаря аннотации `@Primary`, Spring инжектит ему `IdempotentUseCaseDecorator`.
3. `AccountCommandConsumer` вызывает `decorator.processDebitFunds()`.
4. Декоратор выполняет свою работу (проверку в `inbox`), а затем вызывает тот же метод у `decoratedService` (
   `AccountServiceImpl`), где выполняется основная бизнес-логика.

### Выгоды

- **Разделение Ответственности (SoC):** `AccountServiceImpl` отвечает только за бизнес-логику.
  `IdempotentUseCaseDecorator` — только за идемпотентность.
- **Гибкость:** Если нам понадобится `UseCase` без идемпотентности, мы просто инжектим напрямую `AccountServiceImpl` (
  например, через аннотацию `@Qualifier`). Поведение системы меняется через конфигурацию, а не через изменение кода.
- **Чистота:** Ядро бизнес-логики не "загрязнено" техническими деталями.
