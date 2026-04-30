package com.shuzi.managementplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures parent-student relation schema stays compatible with parent management features.
 */
@Component
public class ParentRelationSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(ParentRelationSchemaInitializer.class);
    private static final String TABLE_NAME = "parent_student_relations";
    private static final String BINDING_TYPE_COLUMN = "binding_type";

    private final JdbcTemplate jdbcTemplate;

    public ParentRelationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        ensureBindingTypeColumn();
    }

    private void ensureBindingTypeColumn() {
        if (columnExists(BINDING_TYPE_COLUMN)) {
            return;
        }
        jdbcTemplate.execute(
                "ALTER TABLE parent_student_relations ADD COLUMN binding_type VARCHAR(16) NOT NULL DEFAULT 'AUTO' AFTER student_id"
        );
        log.info("Applied schema patch: {}.{}", TABLE_NAME, BINDING_TYPE_COLUMN);
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
