package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.config.AiGenerationProperties;
import com.shuzi.managementplatform.integration.ai.AiTextClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GeneratedContentService {

    private final AiTextClient aiTextClient;
    private final AiGenerationProperties properties;

    public GeneratedContentService(AiTextClient aiTextClient, AiGenerationProperties properties) {
        this.aiTextClient = aiTextClient;
        this.properties = properties;
    }

    public String generateTrainingSummary(
            String trainingContent,
            String highlightNote,
            String improvementNote,
            String parentAction,
            String nextStepSuggestion
    ) {
        String fallback = "课堂亮点：" + defaultText(highlightNote)
                + "；待改进点：" + defaultText(improvementNote)
                + "；家长可配合：" + defaultText(parentAction)
                + "；下次建议：" + defaultText(nextStepSuggestion);
        return generateWithFallback(
                "你是一名青少年体能训练反馈助手，只输出对家长友好的中文总结。",
                """
                        训练内容：%s
                        课堂亮点：%s
                        待改进点：%s
                        家长配合：%s
                        下次建议：%s
                        """.formatted(
                        defaultText(trainingContent),
                        defaultText(highlightNote),
                        defaultText(improvementNote),
                        defaultText(parentAction),
                        defaultText(nextStepSuggestion)
                ),
                fallback
        );
    }

    public String generateStageInterpretation(
            String trainingSummary,
            String fitnessSummary,
            String coachEvaluation,
            String nextStagePlan
    ) {
        String fallback = "训练完成情况：" + defaultText(trainingSummary)
                + "；体测变化：" + defaultText(fitnessSummary)
                + "；教练评价：" + defaultText(coachEvaluation)
                + "；下阶段计划：" + defaultText(nextStagePlan);
        return generateWithFallback(
                "你是一名青少年体能训练阶段评估助手，请输出简洁、清晰、适合家长阅读的中文说明。",
                """
                        训练完成情况：%s
                        体测变化：%s
                        教练评价：%s
                        下阶段计划：%s
                        """.formatted(
                        defaultText(trainingSummary),
                        defaultText(fitnessSummary),
                        defaultText(coachEvaluation),
                        defaultText(nextStagePlan)
                ),
                fallback
        );
    }

    public String generateParentReport(
            String studentName,
            String cycleName,
            double attendanceRate,
            String fitnessSummary,
            String coachEvaluation,
            String nextStagePlan
    ) {
        String fallback = "学员：" + defaultText(studentName)
                + "；周期：" + defaultText(cycleName)
                + "；出勤率：" + (attendanceRate * 100) + "%"
                + "；体测变化：" + defaultText(fitnessSummary)
                + "；教练评价：" + defaultText(coachEvaluation)
                + "；下阶段计划：" + defaultText(nextStagePlan);
        return generateWithFallback(
                "你是一名青少年体能训练家长报告助手，请输出亲切、专业、简洁的中文阶段报告。",
                """
                        学员：%s
                        周期：%s
                        出勤率：%.1f%%
                        体测变化：%s
                        教练评价：%s
                        下阶段计划：%s
                        """.formatted(
                        defaultText(studentName),
                        defaultText(cycleName),
                        attendanceRate * 100,
                        defaultText(fitnessSummary),
                        defaultText(coachEvaluation),
                        defaultText(nextStagePlan)
                ),
                fallback
        );
    }

    private String generateWithFallback(String systemPrompt, String userPrompt, String fallback) {
        if (!properties.isEnabled()) {
            return fallback;
        }
        try {
            String generated = aiTextClient.complete(systemPrompt, userPrompt);
            return StringUtils.hasText(generated) ? generated.trim() : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "无";
    }
}
