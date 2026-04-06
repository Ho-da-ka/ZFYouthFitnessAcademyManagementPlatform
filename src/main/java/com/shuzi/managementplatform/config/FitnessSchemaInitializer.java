package com.shuzi.managementplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures fitness test table supports logical delete and student detach-on-delete flow.
 */
@Component
public class FitnessSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(FitnessSchemaInitializer.class);
    private static final String TABLE_NAME = "fitness_test_records";

    private final JdbcTemplate jdbcTemplate;

    public FitnessSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        ensureColumn(
                "student_name_snapshot",
                "ALTER TABLE fitness_test_records ADD COLUMN student_name_snapshot VARCHAR(64) NULL AFTER student_id"
        );
        ensureColumn(
                "deleted",
                "ALTER TABLE fitness_test_records ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER comment"
        );
        ensureStudentIdNullable();
        ensureDeletedIndex();
        backfillStudentNameSnapshot();
    }

    private void ensureColumn(String columnName, String ddl) {
        if (columnExists(columnName)) {
            return;
        }
        jdbcTemplate.execute(ddl);
        log.info("Applied schema patch: {}.{}", TABLE_NAME, columnName);
    }

    private void ensureStudentIdNullable() {
        String sql = """
                SELECT IS_NULLABLE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = 'student_id'
                """;
        String nullable = jdbcTemplate.queryForObject(sql, String.class, TABLE_NAME);
        if ("YES".equalsIgnoreCase(nullable)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE fitness_test_records MODIFY COLUMN student_id BIGINT NULL");
        log.info("Applied schema patch: {}.student_id -> NULLABLE", TABLE_NAME);
    }

    private void ensureDeletedIndex() {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = 'idx_fitness_deleted'
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, TABLE_NAME);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("CREATE INDEX idx_fitness_deleted ON fitness_test_records(deleted)");
        log.info("Applied schema patch: add index idx_fitness_deleted");
    }

    private void backfillStudentNameSnapshot() {
        jdbcTemplate.execute("""
                UPDATE fitness_test_records f
                LEFT JOIN students s ON f.student_id = s.id
                SET f.student_name_snapshot = COALESCE(f.student_name_snapshot, s.name)
                WHERE f.student_name_snapshot IS NULL
                """);
    }

    private boolean columnExists(String columnName) {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, TABLE_NAME, columnName);
        return count != null && count > 0;
    }
}

