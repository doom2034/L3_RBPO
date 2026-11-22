-- Демонстрационные записи (ID 90001+, чтобы избежать конфликта с Postman-сценарием).
-- psql -U postgres -d exam_db -v ON_ERROR_STOP=1 -f db/02_seed.sql
-- Повторный запуск не дублирует строки; существующие ID остаются неизменными.
BEGIN;
INSERT INTO users (id, name, email) VALUES
 (90001, 'Мария Демонстрационная', 'maria.seed@example.test'),
 (90002, 'Павел Демонстрационный', 'pavel.seed@example.test')
ON CONFLICT (id) DO NOTHING;
INSERT INTO exams (id, title, passing_score, time_limit, status) VALUES
 (90001, 'Демонстрационный экзамен по SQL', 10, 45, 'ACTIVE'),
 (90002, 'Черновик экзамена по Java', 5, 30, 'DRAFT')
ON CONFLICT (id) DO NOTHING;
INSERT INTO exam_tasks (id, exam_id, description, reference_answer, max_score) VALUES
 (90001, 90001, 'Какой SQL-оператор выбирает строки?', 'SELECT', 10),
 (90002, 90001, 'Какой оператор добавляет строку?', 'INSERT', 10),
 (90003, 90002, 'Какое ключевое слово создаёт класс?', 'class', 5)
ON CONFLICT (id) DO NOTHING;
INSERT INTO exam_attempts (id, exam_id, user_id, start_time, end_time, final_score, status) VALUES
 (90001, 90001, 90001, TIMESTAMP '2026-01-01 12:00:00', TIMESTAMP '2026-01-01 12:05:00', 10, 'PASSED')
ON CONFLICT (id) DO NOTHING;
INSERT INTO task_solutions (id, attempt_id, task_id, student_answer, score) VALUES
 (90001, 90001, 90001, 'SELECT', 10)
ON CONFLICT (id) DO NOTHING;
COMMIT;
-- Следующее значение identity гарантированно выше добавленных ID.
-- Не сбрасываем sequence назад, даже если другие ID уже существуют.
SELECT setval(pg_get_serial_sequence('users','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM users),1));
SELECT setval(pg_get_serial_sequence('exams','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM exams),1));
SELECT setval(pg_get_serial_sequence('exam_tasks','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM exam_tasks),1));
SELECT setval(pg_get_serial_sequence('exam_attempts','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM exam_attempts),1));
SELECT setval(pg_get_serial_sequence('task_solutions','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM task_solutions),1));
