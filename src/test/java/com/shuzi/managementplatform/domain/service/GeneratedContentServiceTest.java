package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.config.AiGenerationProperties;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.integration.ai.AiTextClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratedContentServiceTest {

    @Mock
    private AiTextClient aiTextClient;

    @Test
    void summarizeTrainingShouldFallBackToChineseTemplateWhenClientFails() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setEnabled(true);
        properties.setModel("deepseek-v4-flash");

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

    @Test
    void summarizeTrainingShouldSendReadableChinesePromptToAiClient() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setEnabled(true);
        properties.setModel("deepseek-v4-flash");

        GeneratedContentService service = new GeneratedContentService(aiTextClient, properties);

        when(aiTextClient.complete(anyString(), anyString())).thenReturn("  AI 生成内容  ");

        String summary = service.generateTrainingSummary(
                "agility ladder + jump rope",
                "stable foot cadence",
                "late-session stamina dropped",
                "do two stretching sets tonight",
                "reinforce hip stability next class"
        );

        Assertions.assertEquals("AI 生成内容", summary);

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(aiTextClient).complete(systemPrompt.capture(), userPrompt.capture());

        Assertions.assertTrue(systemPrompt.getValue().contains("青少年体能训练反馈助手"));
        Assertions.assertTrue(userPrompt.getValue().contains("训练内容：agility ladder + jump rope"));
        Assertions.assertTrue(userPrompt.getValue().contains("课堂亮点：stable foot cadence"));
    }

    @Test
    void studentInsightShouldSendRecentTrainingAndFitnessToAiClient() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setEnabled(true);
        properties.setModel("deepseek-v4-flash");

        GeneratedContentService service = new GeneratedContentService(aiTextClient, properties);

        Student student = new Student();
        student.setName("阿斯蒂芬");
        student.setGoalFocus("爆发力提升");
        student.setTrainingTags("跳跃,协调");

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setTrainingDate(LocalDate.of(2026, 4, 6));
        trainingRecord.setTrainingContent("立定跳远专项");
        trainingRecord.setHighlightNote("起跳稳定");
        trainingRecord.setImprovementNote("核心耐力不足");

        FitnessTestRecord fitnessTestRecord = new FitnessTestRecord();
        fitnessTestRecord.setTestDate(LocalDate.of(2026, 4, 6));
        fitnessTestRecord.setItemName("立定跳远");
        fitnessTestRecord.setTestValue(BigDecimal.valueOf(185));
        fitnessTestRecord.setUnit("cm");

        when(aiTextClient.complete(anyString(), anyString())).thenReturn("  DeepSeek 学员洞察  ");

        String insight = service.generateStudentInsightSummary(
                student,
                List.of(trainingRecord),
                List.of(fitnessTestRecord)
        );

        Assertions.assertEquals("DeepSeek 学员洞察", insight);

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(aiTextClient).complete(systemPrompt.capture(), userPrompt.capture());

        Assertions.assertTrue(systemPrompt.getValue().contains("青少年体能训练分析助手"));
        Assertions.assertTrue(userPrompt.getValue().contains("学员：阿斯蒂芬"));
        Assertions.assertTrue(userPrompt.getValue().contains("训练记录：2026-04-06 立定跳远专项"));
        Assertions.assertTrue(userPrompt.getValue().contains("体测记录：2026-04-06 立定跳远 185cm"));
    }

    @Test
    void studentInsightResultShouldExposeWhetherAiGeneratedTheText() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setEnabled(true);
        properties.setModel("deepseek-v4-flash");

        GeneratedContentService service = new GeneratedContentService(aiTextClient, properties);

        Student student = new Student();
        student.setName("阿斯蒂芬");
        student.setGoalFocus("爆发力提升");

        when(aiTextClient.complete(anyString(), anyString())).thenReturn("  DeepSeek 成长解析  ");

        GeneratedContentService.GeneratedText insight = service.generateStudentInsightSummaryResult(
                student,
                List.of(),
                List.of()
        );

        Assertions.assertEquals("DeepSeek 成长解析", insight.text());
        Assertions.assertTrue(insight.generatedByAi());
    }
}
