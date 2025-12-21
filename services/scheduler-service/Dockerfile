# Этап 1: Сборка приложения
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Копируем скрипты и файлы сборки
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew

# Копируем исходный код
COPY common ./common
COPY services/scheduler-service ./services/scheduler-service

# Собираем JAR-файл
RUN ./gradlew :services:scheduler-service:bootJar -x test

# Этап 2: Создание легковесного образа
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/services/scheduler-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]