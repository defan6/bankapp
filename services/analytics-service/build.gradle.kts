// services/analytics-service/build.gradle.kts
dependencies {
    implementation(project(":common"))
    implementation("org.springframework.kafka:spring-kafka")
    // Драйвер для ClickHouse
    implementation("com.clickhouse:clickhouse-jdbc:0.5.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
