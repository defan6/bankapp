#!/bin/bash
set -e

# Эта функция вызывается официальным entrypoint скриптом Postgres.
# Она использует переменные окружения POSTGRES_USER и POSTGRES_DB из docker-compose.yml.
# Мы подключаемся к основной БД ($POSTGRES_DB) от имени суперпользователя ($POSTGRES_USER) 
# и создаем новые БД и роли для каждого микросервиса.

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- User Service
    CREATE USER user_service_user WITH PASSWORD 'password';
    CREATE DATABASE user_service_db;
    GRANT ALL PRIVILEGES ON DATABASE user_service_db TO user_service_user;

    -- Account Service
    CREATE USER account_service_user WITH PASSWORD 'password';
    CREATE DATABASE account_service_db;
    GRANT ALL PRIVILEGES ON DATABASE account_service_db TO account_service_user;

    -- Transaction Service
    CREATE USER transaction_service_user WITH PASSWORD 'password';
    CREATE DATABASE transaction_service_db;
    GRANT ALL PRIVILEGES ON DATABASE transaction_service_db TO transaction_service_user;

    -- History Service
    CREATE USER history_service_user WITH PASSWORD 'password';
    CREATE DATABASE history_service_db;
    GRANT ALL PRIVILEGES ON DATABASE history_service_db TO history_service_user;

    -- Scheduler Service
    CREATE USER scheduler_service_user WITH PASSWORD 'password';
    CREATE DATABASE scheduler_service_db;
    GRANT ALL PRIVILEGES ON DATABASE scheduler_service_db TO scheduler_service_user;
EOSQL
