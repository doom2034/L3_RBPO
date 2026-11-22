-- Проверка установленной схемы / примеров (не изменяет данные).
-- psql -U postgres -d exam_db -f db/03_verify.sql
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT 'exams', COUNT(*) FROM exams
UNION ALL SELECT 'exam_tasks', COUNT(*) FROM exam_tasks
UNION ALL SELECT 'exam_attempts', COUNT(*) FROM exam_attempts
UNION ALL SELECT 'task_solutions', COUNT(*) FROM task_solutions;
SELECT e.id, e.title, COUNT(t.id) AS tasks_count
FROM exams e LEFT JOIN exam_tasks t ON t.exam_id=e.id GROUP BY e.id,e.title ORDER BY e.id;
SELECT a.id, u.name, e.title, a.status, a.final_score
FROM exam_attempts a JOIN users u ON a.user_id=u.id JOIN exams e ON a.exam_id=e.id ORDER BY a.id;
SELECT has_table_privilege('exam_app','public.exams','SELECT') AS can_select_exams,
       has_table_privilege('exam_app','public.exams','INSERT') AS can_insert_exams,
       has_schema_privilege('exam_app','public','CREATE') AS can_create_tables;
-- Ожидается: can_select_exams=true, can_insert_exams=true, can_create_tables=false.
