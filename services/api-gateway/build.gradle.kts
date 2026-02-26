// services/api-gateway/build.gradle.kts

// Исключаем starter-web, так как он несовместим с реактивным Gateway.
// Он добавляется для всех модулей в корневом build.gradle.kts.
configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-web")
    }
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") // Для Rate Limiter
    // Используем webflux-ui, так как Gateway работает на WebFlux, а не на MVC
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")


    // Observability with OTLP
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.micrometer:micrometer-registry-otlp")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.23.0-alpha")
}
