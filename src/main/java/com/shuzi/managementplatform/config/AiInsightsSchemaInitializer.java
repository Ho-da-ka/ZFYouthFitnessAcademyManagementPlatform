package com.shuzi.managementplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures students table has ai_insights column for AI Student Insights persistence.
 */
@Component
public class AiInsightsSchemaInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AiInsightsSchemaInitializer.class);
    private static final String TABLE_NAME = "students";
    private static final String COLUMN_NAME = "ai_insights";

    private final JdbcTemplate jdbcTemplate;

    public AiInsightsSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        if (!columnExists()) {
            jdbcTemplate.execute("ALTER TABLE students ADD COLUMN ai_insights TEXT NULL");
            log.info("Applied schema patch: {}.{}", TABLE_NAME, COLUMN_NAME);
        }
    }

    private boolean columnExists() {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, TABLE_NAME, COLUMN_NAME);
        return count != null && count > 0;
    }
}
