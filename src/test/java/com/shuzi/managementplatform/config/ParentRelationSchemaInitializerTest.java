package com.shuzi.managementplatform.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentRelationSchemaInitializerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void onReadyShouldAddBindingTypeColumnWhenMissing() {
        when(jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                "parent_student_relations",
                "binding_type"
        )).thenReturn(0);

        ParentRelationSchemaInitializer initializer = new ParentRelationSchemaInitializer(jdbcTemplate);

        initializer.onReady();

        verify(jdbcTemplate).execute(
                "ALTER TABLE parent_student_relations ADD COLUMN binding_type VARCHAR(16) NOT NULL DEFAULT 'AUTO' AFTER student_id"
        );
    }

    @Test
    void onReadyShouldSkipBindingTypeColumnWhenPresent() {
        when(jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                "parent_student_relations",
                "binding_type"
        )).thenReturn(1);

        ParentRelationSchemaInitializer initializer = new ParentRelationSchemaInitializer(jdbcTemplate);

        initializer.onReady();

        verify(jdbcTemplate, never()).execute(
                "ALTER TABLE parent_student_relations ADD COLUMN binding_type VARCHAR(16) NOT NULL DEFAULT 'AUTO' AFTER student_id"
        );
    }
}
