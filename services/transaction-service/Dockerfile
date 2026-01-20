# Этап 1: Сборка приложения
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Копируем скрипты и файлы сборки для кэширования зависимостей
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY common/build.gradle.kts ./common/build.gradle.kts
COPY services/transaction-service/build.gradle.kts ./services/transaction-service/build.gradle.kts
RUN chmod +x gradlew

# Загружаем зависимости, чтобы они были в отдельном кэшируемом слое
RUN ./gradlew :services:transaction-service:dependencies

# Копируем исходный код
COPY common ./common
COPY services/transaction-service ./services/transaction-service
COPY contracts ./contracts

# Собираем JAR-файл
RUN ./gradlew :services:transaction-service:bootJar -x test

# Этап 2: Создание легковесного образа
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/services/transaction-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]