// Корневой build.gradle.kts
// Здесь определяются общие для всех подпроектов зависимости и конфигурации.
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    // Применяем плагины ко всем подпроектам
    base
    id("java")
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4"
}

// Явно указываем версию Gradle для Wrapper'a
tasks.wrapper {
    gradleVersion = "8.14"
}

allprojects {
    group = "com.bankapp"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        // ДОБАВЛЯЕМ ЭТОТ РЕПОЗИТОРИЙ
        maven { url = uri("https://packages.confluent.io/maven/") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    dependencyManagement {
        imports {
            mavenBom(SpringBootPlugin.BOM_COORDINATES)
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
        }
    }
    
    dependencies {
        // Зависимости, общие для всех сервисов
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-actuator") // Для метрик и health-check'ов
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
