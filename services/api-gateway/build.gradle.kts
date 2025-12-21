// services/api-gateway/build.gradle.kts
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") // Для Rate Limiter
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
