# План внедрения кодогенерации (v2)

**Цель:** Интегрировать в проект механизмы кодогенерации для API-клиентов (OpenAPI/OpenFeign) и моделей событий (Avro). Это повысит надежность, ускорит разработку и создаст единый источник правды для контрактов.

---

### Часть 1: API-контракты (OpenAPI & Feign)

Здесь мы сфокусируемся на синхронном взаимодействии между сервисами, например, на вызове `transaction-service` -> `account-service`.

#### Шаг 1: Создание источника правды — `openapi.yaml`

Вместо того чтобы хранить описание API в `.md` файле, мы создадим машиночитаемую спецификацию в формате **OpenAPI 3.0**.

**План действий:**
1.  Создать новую директорию для хранения всех контрактов: `contracts/api/`.
2.  **Структура и именование:** Мы договорились использовать **отдельный файл для каждого сервиса**. Предлагается следующая структура: `contracts/api/{service-name}/{service-name}-api.v1.yaml`. Например: `contracts/api/account-service/account-service-api.v1.yaml`.
3.  **Задача:** Вручную перенести в этот YAML-файл описание эндпоинтов `account-service` из `cbs-services-api-specification.md`.

**Темы для рассуждения (решения):**
*   **Общие компоненты (DTO):** Чтобы не дублировать общие модели (например, `ErrorDto`), создадим файл `contracts/api/common/common.v1.yaml`. Сервисные спецификации будут ссылаться на него через `$ref: '../common/common.v1.yaml#/components/schemas/ErrorDto'`. **Решение: Принято.**
*   **Версионирование API:** Будем указывать версию в имени файла (`.v1.yaml`). В будущем это также может отразиться в URL (`/api/v1/...`). **Решение: Принято.**

#### Шаг 2: Настройка Gradle для генерации Feign-клиентов

Мы будем использовать плагин `openapi-generator-gradle-plugin` для автоматической генерации кода.

**План действий:**
1.  Подключить плагин в `build.gradle.kts` модуля `common`.
2.  Настроить плагин. Примерная конфигурация:
    ```kotlin
    // в common/build.gradle.kts
    openApiGenerate {
        generatorName.set("spring")
        inputSpec.set("$rootDir/contracts/api/account-service/account-service-api.v1.yaml")
        outputDir.set("$buildDir/generated/sources/openapi")
        apiPackage.set("com.bankapp.common.client.account.api")
        modelPackage.set("com.bankapp.common.client.account.model")
        configOptions.set(mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "java8" to "false",
            "dateLibrary" to "java17",
            "serializationLibrary" to "jackson"
        ))
    }
    ```
3.  Добавить сгенерированные исходники в `sourceSets`.

#### Шаг 3: Интеграция сгенерированного клиента

**План действий:**
1.  Удалить вручную созданный `AccountServiceClient.java` в `common`.
2.  Обновить `transaction-service`, чтобы он использовал новый, сгенерированный интерфейс.

---

### Часть 2: События (Avro & Schema Registry)

Здесь мы переводим асинхронное общение через Kafka на рельсы Avro.

#### Шаг 1: Создание Avro-схем (`.avsc`)

**План действий:**
1.  Создать директорию `contracts/events/`.
2.  **Структура и именование:** Предлагается структура `contracts/events/{domain}/{EventName}.v1.avsc`. Например: `contracts/events/user/UserRegistered.v1.avsc`.
3.  **Задача:** Для каждого события из `event-streaming-contracts.md` создать соответствующий `.avsc` файл.

**Темы для рассуждения (решения):**
*   **Общие поля в событиях:** В Avro нет простого наследования. Чтобы сохранить схемы самодостаточными, стандартные поля (`eventId`, `timestamp`, `correlationId`) будут дублироваться в каждой схеме. **Решение: Принято.**

#### Шаг 2: Настройка Gradle для генерации Java-классов

Используем плагин `com.github.davidmc24.gradle.plugin.avro`.

**План действий:**
1.  Подключить и настроить плагин в `build.gradle.kts` каждого сервиса, работающего с событиями.
2.  Плагин будет брать `.avsc` файлы и генерировать Java-классы в `build/generated/sources/avro/` каждого сервиса.

#### Шаг 3: Внедрение Реестра Схем (Schema Registry)

**План действий:**
1.  Добавить сервис Schema Registry от Confluent в `docker-compose.yml`.
2.  В `application.yml` каждого сервиса поменять сериализаторы на `io.confluent.kafka.serializers.KafkaAvroSerializer` / `KafkaAvroDeserializer` и указать URL реестра схем (`schema.registry.url`).

---

### Часть 3: Процесс работы и версионирование

Этот раздел описывает пошаговый процесс для разработчика, который хочет внести изменения в контракт.

1.  **Изменение контракта:** Разработчик вносит правки в соответствующий `.yaml` или `.avsc` файл в директории `contracts`.
2.  **Локальная кодогенерация:** Разработчик запускает локально gradle-таску (например, `./gradlew :common:generateopenapi`) для генерации нового кода на основе измененного контракта.
3.  **Адаптация бизнес-логики:** Разработчик исправляет свой код, чтобы он соответствовал новым сгенерированным интерфейсам или моделям. Компилятор будет его лучшим помощником на этом этапе.
4.  **Тестирование:** Разработчик запускает тесты, чтобы убедиться, что ничего не сломалось.
5.  **Коммит:** Все изменения — правка контракта, новый сгенерированный код и адаптация бизнес-логики — коммитятся вместе, в одном Pull Request. Это гарантирует, что репозиторий всегда находится в консистентном состоянии.

---
### Общий пошаговый план к действию (Action Plan)

1.  Создать директории `contracts/api/{service-name}` и `contracts/events/{domain}`.
2.  Создать `contracts/api/common/common.v1.yaml` для общих DTO.
3.  Создать `contracts/api/account-service/account-service-api.v1.yaml` и описать в нем эндпоинты.
4.  Настроить `openapi-generator-gradle-plugin` в `common/build.gradle.kts`.
5.  Создать `contracts/events/user/UserRegistered.v1.avsc`.
6.  Настроить avro-плагин в `user-service` и `notification-service`.
7.  Добавить Schema Registry в `docker-compose.yml` и обновить конфигурацию Kafka в сервисах.
8.  Проверить и заменить ручной код на сгенерированный.


Этот план позволит нам итерационно, шаг за шагом, внедрить кодогенерацию, не ломая все сразу, и сразу же получать от этого пользу.