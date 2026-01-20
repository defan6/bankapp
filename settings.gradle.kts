// Файл settings.gradle.kts
// Определяет все подпроекты, которые входят в нашу сборку.

rootProject.name = "bankapp"

include(
    "common",
    "services:api-gateway",
    "services:user-service",
    "services:account-service",
    "services:transaction-service",
    "services:history-service",
    "services:notification-service",
    "services:analytics-service",
    "services:scheduler-service"
)
