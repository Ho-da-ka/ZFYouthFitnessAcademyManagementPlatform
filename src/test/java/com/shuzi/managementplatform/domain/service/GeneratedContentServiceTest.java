package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.config.AiGenerationProperties;
import com.shuzi.managementplatform.integration.ai.AiTextClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratedContentServiceTest {

    @Mock
    private AiTextClient aiTextClient;

    @Test
    void summarizeTrainingShouldFallBackToTemplateWhenClientFails() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setEnabled(true);
        properties.setModel("gpt-4o-mini");

        GeneratedContentService service = new GeneratedContentService(aiTextClient, properties);

        when(aiTextClient.complete(anyString(), anyString())).thenThrow(new IllegalStateException("down"));

        String summary = service.generateTrainingSummary(
                "agility ladder + jump rope",
                "stable foot cadence",
                "late-session stamina dropped",
                "do two stretching sets tonight",
                "reinforce hip stability next class"
        );

        Assertions.assertTrue(summary.contains("课堂亮点"));
        Assertions.assertTrue(summary.contains("家长可配合"));
    }
}
