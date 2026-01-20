# План внедрения Observability (Стек Grafana: PLT + Alloy)

**Цель:** Обеспечить прозрачность работы системы с помощью современного, интегрированного стека: Prometheus (метрики),
Loki (логи), Tempo (трейсы), с использованием Grafana Alloy в качестве единого агента-коллектора.

**Основная идея:** Приложения отправляют всю телеметрию (метрики, логи, трейсы) в едином формате **OTLP (OpenTelemetry
Protocol)** в **Grafana Alloy**. Alloy, в свою очередь, распределяет эти данные по соответствующим хранилищам (
Prometheus, Loki, Tempo).

---

### 1. Трассировка и метрики через OTLP

**Идея:** Настроить все сервисы на экспорт метрик и трейсов в формате OTLP, вместо специфичных для Prometheus или Zipkin
форматов.

**Задачи:**

1. **Изменить зависимости (в `build.gradle.kts` каждого сервиса):**
    * Убедиться в наличии:
        * `org.springframework.boot:spring-boot-starter-actuator`
        * `io.micrometer:micrometer-tracing-bridge-brave` (или `otel`)
    * **Удалить:** `micrometer-registry-prometheus` и `zipkin-reporter-brave` (если они были).
    * **Добавить:** `io.micrometer:micrometer-registry-otlp` — это новый "реестр", который будет отправлять метрики по
      протоколу OTLP.
2. **Обновить конфигурацию (`application.yml` каждого сервиса):**
    * Настроить экспорт метрик и трейсов в Alloy. Эндпоинт `/actuator/prometheus` больше не нужен.

   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info # Эндпоинт prometheus больше не выставляется наружу
     tracing:
       sampling:
         probability: 1.0 # Собираем все трейсы для отладки
     otlp:
       # Общий эндпоинт для OTLP/HTTP, указывающий на контейнер с Alloy
       endpoint: http://alloy:4318 
       metrics:
         export:
           # Стандартный путь для метрик в OTLP/HTTP
           url: ${management.otlp.endpoint}/v1/metrics 
       tracing:
         export:
           # Стандартный путь для трейсов в OTLP/HTTP
           url: ${management.otlp.endpoint}/v1/traces
   ```

**Результат (Breakpoint):** Сервис при запуске не предоставляет эндпоинт `/actuator/prometheus`, а пытается отправить
метрики и трейсы по HTTP на адрес `http://alloy:4318`.

---

### 2. Логирование в Loki через OTLP

**Идея:** Настроить логирование так, чтобы логи отправлялись в Alloy (и далее в Loki), автоматически обогащаясь
`traceId` и `spanId` для идеальной корреляции с трассами в Grafana.

**Задачи:**

1. **Изменить зависимости:** Вместо `logstash-logback-encoder` использовать зависимость, предоставляющую
   Logback-аппендер для OTLP, например, `io.opentelemetry:opentelemetry-logback-appender-1.0`.
2. **Настроить `logback-spring.xml`:** Настроить `OtlpHttpLoggingAppender` (или gRPC-аналог), который будет отправлять
   логи на OTLP-приемник Alloy (например, `http://alloy:4318/v1/logs`). Этот аппендер автоматически свяжет логи с
   текущим трейсом.

**Результат (Breakpoint):** Логи приложения не только пишутся в консоль, но и отправляются по сети в Alloy. В Grafana (
во вкладке Explore -> Loki) можно увидеть эти логи, и они содержат `trace_id`.

---

### 3. Настройка компонентов в Docker Compose

**Идея:** Добавить в `docker-compose.yml` все необходимые сервисы и настроить их взаимодействие.

**Задачи:**

1. **Добавить сервисы в `docker-compose.yml`:**
    * `prometheus`: Хранилище метрик.
    * `loki`: Хранилище логов.
    * `tempo`: Хранилище трейсов.
    * `grafana`: UI для визуализации.
    * `alloy`: Наш коллектор.
2. **Настроить Alloy:**
    * Создать конфигурационный файл для Alloy, например, `observability/alloy-config.river`.
    * В этом файле описать "пайплайн":
        * **Приемник (receiver):** Настроить `otelcol.receiver.otlp` для приема данных от наших сервисов.
        * **Экспортеры (exporters):** Настроить экспорт принятых данных: `otelcol.exporter.prometheus` для метрик,
          `loki.write` для логов, `otelcol.exporter.otlp` для трейсов в Tempo.
        * Связать приемники с экспортерами.
    * Смонтировать этот файл конфигурации в контейнер `alloy`.
3. **Настроить Grafana:** В конфигурации Grafana (например, через `provisioning/datasources`) добавить Prometheus, Loki
   и Tempo как источники данных (Data Sources), чтобы они были доступны сразу после старта.

**Результат (Breakpoint):** После запуска `docker-compose up` в Grafana (обычно `http://localhost:3000`) можно зайти во
вкладку "Explore" и успешно выполнить запросы к метрикам из Prometheus, логам из Loki и трейсам из Tempo. При просмотре
трейса есть возможность перейти к связанным с ним логам.
