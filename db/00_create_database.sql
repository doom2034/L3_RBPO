-- Выполнять через psql от администратора PostgreSQL, НЕ от exam_app.
-- psql -U postgres -d postgres -v ON_ERROR_STOP=1 -f db/00_create_database.sql
-- Пароль пользователя приложения задаётся отдельно интерактивной командой psql \password exam_app.
SELECT 'CREATE ROLE exam_app LOGIN' WHERE NOT EXISTS (
  SELECT 1 FROM pg_roles WHERE rolname = 'exam_app'
) \gexec
SELECT 'CREATE DATABASE exam_db' WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = 'exam_db'
) \gexec
REVOKE ALL ON DATABASE exam_db FROM PUBLIC;
GRANT CONNECT ON DATABASE exam_db TO exam_app;
-- После этого отдельно выполните: sudo -iu postgres psql -d postgres -c "\password exam_app"
