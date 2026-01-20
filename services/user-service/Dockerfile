# Этап 1: Сборка приложения
FROM eclipse-temurin:17-jdk-jammy AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем скрипты и файлы сборки для кэширования зависимостей
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY common/build.gradle.kts ./common/build.gradle.kts
COPY services/user-service/build.gradle.kts ./services/user-service/build.gradle.kts
RUN chmod +x gradlew

# Загружаем зависимости, чтобы они были в отдельном кэшируемом слое
RUN ./gradlew :services:user-service:dependencies

# Копируем исходный код общего модуля и нашего сервиса
COPY common ./common
COPY services/user-service ./services/user-service
COPY contracts ./contracts

# Собираем JAR-файл только для конкретного сервиса (bootJar)
RUN ./gradlew :services:user-service:bootJar -x test

# Этап 2: Создание легковесного образа
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/services/user-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]