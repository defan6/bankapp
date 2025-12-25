#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-'EOSQL'

-- ============================================================
-- USERS (можно в DO)
-- ============================================================
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'user_service_user') THEN
      CREATE ROLE user_service_user LOGIN PASSWORD 'password';
   END IF;

   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'account_service_user') THEN
      CREATE ROLE account_service_user LOGIN PASSWORD 'password';
   END IF;

   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'transaction_service_user') THEN
      CREATE ROLE transaction_service_user LOGIN PASSWORD 'password';
   END IF;

   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'history_service_user') THEN
      CREATE ROLE history_service_user LOGIN PASSWORD 'password';
   END IF;

   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'scheduler_service_user') THEN
      CREATE ROLE scheduler_service_user LOGIN PASSWORD 'password';
   END IF;
END
$$;

-- ============================================================
-- DATABASES (ТОЛЬКО через gexec)
-- ============================================================

SELECT format(
  'CREATE DATABASE user_service_db OWNER user_service_user'
)
WHERE NOT EXISTS (
  SELECT FROM pg_database WHERE datname = 'user_service_db'
)\gexec

SELECT format(
  'CREATE DATABASE account_service_db OWNER account_service_user'
)
WHERE NOT EXISTS (
  SELECT FROM pg_database WHERE datname = 'account_service_db'
)\gexec

SELECT format(
  'CREATE DATABASE transaction_service_db OWNER transaction_service_user'
)
WHERE NOT EXISTS (
  SELECT FROM pg_database WHERE datname = 'transaction_service_db'
)\gexec

SELECT format(
  'CREATE DATABASE history_service_db OWNER history_service_user'
)
WHERE NOT EXISTS (
  SELECT FROM pg_database WHERE datname = 'history_service_db'
)\gexec

SELECT format(
  'CREATE DATABASE scheduler_service_db OWNER scheduler_service_user'
)
WHERE NOT EXISTS (
  SELECT FROM pg_database WHERE datname = 'scheduler_service_db'
)\gexec

EOSQL