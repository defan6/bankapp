// services/user-service/build.gradle.kts
plugins {
    // Плагины 'java', 'org.springframework.boot', 'io.spring.dependency-management' применяются из корневого build.gradle.kts
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0" // Добавляем плагин Avro
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.liquibase:liquibase-core")
    // Библиотеки для работы с JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")


}

tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    source("$rootDir/contracts/events/user")
}
// Добавляем директорию сгенерированных исходников в основной набор исходников
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/sources/avro")) // Директория вывода для Avro плагина
        }
    }
}

// Гарантируем, что задача generateAvroJava выполняется перед compileJava
tasks.named("compileJava") {
    dependsOn(tasks.named("generateAvroJava"))
}
