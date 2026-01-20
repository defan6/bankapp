import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.Locale

plugins {
    id("java-library")
    id("org.openapi.generator") version "7.0.0"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.8.0"
}

tasks.named("bootJar") {
    enabled = false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

/* ---------------- DEPENDENCIES ---------------- */

dependencies {
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation("jakarta.validation:jakarta.validation-api")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.16")

    implementation("io.confluent:kafka-avro-serializer:7.5.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

/* ---------------- OPENAPI GENERATION ---------------- */

val openApiOutputDir = layout.buildDirectory.dir("generated/sources/openapi")
val openApiTasks = mutableListOf<TaskProvider<GenerateTask>>()

fileTree("$rootDir/contracts/api") {
    include("**/*-api.v1.yaml")
    exclude("**/common/**")
}.files.forEach { specFile ->

    val serviceDir = specFile.parentFile.name // account-service
    val servicePackage = serviceDir.replace("-", "")
    val taskSuffix = serviceDir
        .split("-")
        .joinToString("") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }

    val task = tasks.register<GenerateTask>("generate${taskSuffix}Api") {
        group = "openapi"
        description = "Generate OpenAPI client for $serviceDir"

        generatorName.set("spring")
        inputSpec.set(specFile.absolutePath)
        outputDir.set(openApiOutputDir.map { it.dir(serviceDir).asFile.path })

        apiPackage.set("com.bankapp.common.client.$servicePackage.api")
        modelPackage.set("com.bankapp.common.client.$servicePackage.model")

        apiNameSuffix.set("ApiV1") // suffix

        configOptions.set(
            mapOf(
                "interfaceOnly" to "true",
                "library" to "spring-cloud",
                "useSpringBoot3" to "true",
                "useJakartaEe" to "true",
                "dateLibrary" to "java17",
                "serializationLibrary" to "jackson",
                "useTags" to "true",
                "useBeanValidation" to "true"
                // User, Account from contracts *.yaml
            )
        )

        globalProperties.set(
            mapOf(
                "apiDocs" to "false",
                "modelDocs" to "false",
                "apiTests" to "false",
                "modelTests" to "false"
            )
        )
    }

    openApiTasks += task

    // Lazily add the generated sources to the source set
    sourceSets.main.get().java.srcDir(
        openApiOutputDir.map { it.dir(serviceDir).dir("src/main/java") }
    )
}

tasks.withType<GenerateTask> {
    apiNameSuffix.set("ApiV1")
}

/* ---------------- AVRO GENERATION ---------------- */

tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    source("$rootDir/contracts/events")
}

/* ---------------- SOURCES ---------------- */

sourceSets {
    main {
        java {
            // Avro sources
            srcDir(layout.buildDirectory.dir("generated/sources/avro/java"))
        }
    }
}

/* ---------------- TASK ORDER ---------------- */

tasks.named("compileJava") {
    dependsOn(openApiTasks)
    dependsOn("generateAvroJava")
}
