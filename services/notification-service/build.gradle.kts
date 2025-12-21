// services/notification-service/build.gradle.kts
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.kafka:spring-kafka")
    // implementation("org.springframework.boot:spring-boot-starter-mail") // Раскомментировать для отправки email
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}