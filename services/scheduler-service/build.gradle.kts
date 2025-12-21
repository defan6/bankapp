// services/scheduler-service/build.gradle.kts
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Зависимость для Quartz - планировщика задач
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
