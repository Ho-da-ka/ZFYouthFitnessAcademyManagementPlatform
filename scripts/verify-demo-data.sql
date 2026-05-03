SELECT 'coaches' AS table_name, COUNT(*) AS demo_count FROM coaches WHERE coach_code LIKE 'DEMO-C%'
UNION ALL
SELECT 'students', COUNT(*) FROM students WHERE student_no LIKE 'DEMO-S%'
UNION ALL
SELECT 'courses', COUNT(*) FROM courses WHERE course_code LIKE 'DEMO-%'
UNION ALL
SELECT 'training_records', COUNT(*)
FROM training_records t JOIN students s ON s.id = t.student_id
WHERE s.student_no LIKE 'DEMO-S%'
UNION ALL
SELECT 'fitness_test_records', COUNT(*)
FROM fitness_test_records f JOIN students s ON s.id = f.student_id
WHERE s.student_no LIKE 'DEMO-S%' AND f.deleted = 0
UNION ALL
SELECT 'attendance_records', COUNT(*)
FROM attendance_records a JOIN students s ON s.id = a.student_id
WHERE s.student_no LIKE 'DEMO-S%'
UNION ALL
SELECT 'stage_evaluations', COUNT(*)
FROM stage_evaluations e JOIN students s ON s.id = e.student_id
WHERE s.student_no LIKE 'DEMO-S%'
UNION ALL
SELECT 'course_bookings', COUNT(*)
FROM course_bookings b JOIN students s ON s.id = b.student_id
WHERE s.student_no LIKE 'DEMO-S%'
UNION ALL
SELECT 'in_app_messages', COUNT(*)
FROM in_app_messages m
JOIN parent_accounts pa ON pa.id = m.parent_account_id
JOIN parent_student_relations r ON r.parent_account_id = pa.id
JOIN students s ON s.id = r.student_id
WHERE s.student_no LIKE 'DEMO-S%';

SELECT s.student_no, s.name, CONCAT('student_', LOWER(s.student_no)) AS login_username, c.name AS latest_course, t.training_date, t.highlight_note
FROM students s
LEFT JOIN training_records t ON t.student_id = s.id
LEFT JOIN courses c ON c.id = t.course_id
WHERE s.student_no LIKE 'DEMO-S%'
ORDER BY s.student_no, t.training_date DESC
LIMIT 12;
