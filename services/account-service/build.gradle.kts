
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")


    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // MapStruct
    compileOnly("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")


    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Kafka Avro Serializer/Deserializer
    implementation("io.confluent:kafka-avro-serializer:7.5.1")

    // Observability with OTLP
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.micrometer:micrometer-registry-otlp")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.23.0-alpha")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}


tasks.named("compileJava") {
    dependsOn(project(":common").tasks.matching { it.group == "openapi" })
    dependsOn(":common:generateAvroJava")
}

val integrationTest by sourceSets.creating {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")

    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

configurations {
    named("integrationTestImplementation") {
        extendsFrom(testImplementation.get())
    }
    named("integrationTestRuntimeOnly") {
        extendsFrom(testRuntimeOnly.get())
    }
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTestTask)
}




