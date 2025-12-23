// common/build.gradle.kts
plugins {
    id("java-library") // Явно объявляем, что это библиотечный модуль
    id("org.openapi.generator") version "7.0.0" // Добавляем плагин OpenAPI Generator
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0"
}

// Этот проект будет JAR-файлом, а не исполняемым Spring Boot приложением.
// Поэтому отключаем стандартную задачу сборки Spring Boot.
tasks.getByName("bootJar") {
    enabled = false
}

dependencies {
    // Зависимость от Kafka, чтобы определять события
    implementation("org.springframework.kafka:spring-kafka")
    // Зависимость от JPA для общих Entities, если они будут
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Для Feign Client интерфейсов
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // Дополнительные зависимости, требуемые сгенерированным Feign-клиентом
    // spring-boot-starter-web уже подключен для всех подпроектов в корневом build.gradle.kts
    implementation("jakarta.validation:jakarta.validation-api") // Для аннотаций валидации
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.16") // Для аннотаций @Schema и т.д.



    implementation("io.confluent:kafka-avro-serializer:7.5.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
}

// Конфигурация OpenAPI Generator
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/contracts/api/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated/sources/openapi")
    apiPackage.set("com.bankapp.common.client.api")
    modelPackage.set("com.bankapp.common.client.model")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "java8" to "false",
        "dateLibrary" to "java17",
        "serializationLibrary" to "jackson"
    ))
}


tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    source("$rootDir/contracts/events")
}

// Добавляем директорию сгенерированных исходников в основной набор исходников
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/sources/openapi/src/main/java"))
            srcDir(layout.buildDirectory.dir("generated/sources/avro/java"))
        }
    }
}

// Гарантируем, что задача openApiGenerate выполняется перед compileJava
tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
    dependsOn(tasks.named("generateAvroJava"))
}
