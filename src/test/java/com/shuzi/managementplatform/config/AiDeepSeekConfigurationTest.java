package com.shuzi.managementplatform.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class AiDeepSeekConfigurationTest {

    @Test
    void applicationPropertiesShouldDefaultToDeepSeekWithoutCommittingASecret() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Assertions.assertNotNull(inputStream);
            properties.load(inputStream);
        }

        Assertions.assertEquals("${DEEPSEEK_AI_ENABLED:true}", properties.getProperty("app.ai.enabled"));
        Assertions.assertEquals("${DEEPSEEK_BASE_URL:https://api.deepseek.com}", properties.getProperty("app.ai.base-url"));
        Assertions.assertEquals("${DEEPSEEK_API_KEY:}", properties.getProperty("app.ai.api-key"));
        Assertions.assertEquals("${DEEPSEEK_MODEL:deepseek-v4-flash}", properties.getProperty("app.ai.model"));
        Assertions.assertFalse(properties.getProperty("app.ai.api-key").startsWith("sk-"));
    }
}
