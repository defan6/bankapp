// services/notification-service/build.gradle.kts
dependencies {
    implementation(project(":common")){
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
    }
    implementation("org.springframework.kafka:spring-kafka")
    // implementation("org.springframework.boot:spring-boot-starter-mail") // Раскомментировать для отправки email
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

tasks.named("compileJava") {
    dependsOn(project(":common").tasks.matching { it.group == "openapi" })
}