// common/build.gradle.kts
// Этот проект будет JAR-файлом, а не исполняемым Spring Boot приложением.
// Поэтому отключаем стандартную задачу сборки Spring Boot.
tasks.getByName("bootJar") {
    enabled = false
}

// Этот модуль может содержать общие DTO, события, исключения и т.д.
dependencies {
    // Зависимость от Kafka, чтобы определять события
    implementation("org.springframework.kafka:spring-kafka")
    // Зависимость от JPA для общих Entities, если они будут
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Для Feign Client интерфейсов
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}
