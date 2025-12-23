# Этап 1: Сборка приложения
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Копируем скрипты и файлы сборки для кэширования зависимостей
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY common/build.gradle.kts ./common/build.gradle.kts
COPY services/history-service/build.gradle.kts ./services/history-service/build.gradle.kts
RUN chmod +x gradlew

# Загружаем зависимости, чтобы они были в отдельном кэшируемом слое
RUN ./gradlew :services:history-service:dependencies

# Копируем исходный код
COPY common ./common
COPY services/history-service ./services/history-service
COPY contracts ./contracts

# Собираем JAR-файл
RUN ./gradlew :services:history-service:bootJar -x test

# Этап 2: Создание легковесного образа
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/services/history-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]