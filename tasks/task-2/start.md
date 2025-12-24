1. В корне проекта делаем `docker-compose up -d`. Поднимаем все контейнеры
2. В корне проекта выполняем команду `make build`. Убеждаемся в том что сборка прошла успешно.
3. Заходим в класс `UserControllerV1` в сервисе user-service `comtroller/UserControllerV1` и убеждаемся в том что UserApiV1 успешно импортировалось. У нас нет никаких ошибок компиляции.
4. Все необходимые dto лежат по пути `common/build/generated/openapi/user-service/src/main/java/com.bankapp.common.client.userservice/model`