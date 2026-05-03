package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.config.AiGenerationProperties;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.integration.ai.AiTextClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class GeneratedContentService {

    public record GeneratedText(String text, boolean generatedByAi) {}

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

    public String generateStudentInsightSummary(
            Student student,
            List<TrainingRecord> recentTrainingRecords,
            List<FitnessTestRecord> recentFitnessRecords
    ) {
        return generateStudentInsightSummaryResult(student, recentTrainingRecords, recentFitnessRecords).text();
    }

    public GeneratedText generateStudentInsightSummaryResult(
            Student student,
            List<TrainingRecord> recentTrainingRecords,
            List<FitnessTestRecord> recentFitnessRecords
    ) {
        String fallback = defaultText(student.getName()) + "近期已有 "
                + safeSize(recentTrainingRecords) + " 条训练反馈、"
                + safeSize(recentFitnessRecords) + " 条体测记录。建议围绕"
                + defaultText(student.getGoalFocus()) + "持续跟踪变化，并结合教练反馈安排下一阶段训练。";
        return generateWithFallbackResult(
                "你是一名青少年体能训练分析助手，请基于真实训练和体测数据输出给家长看的中文成长洞察，最多120字。",
                """
                        学员：%s
                        目标：%s
                        标签：%s
                        风险备注：%s
                        训练记录：%s
                        体测记录：%s
                        """.formatted(
                        defaultText(student.getName()),
                        defaultText(student.getGoalFocus()),
                        defaultText(student.getTrainingTags()),
                        defaultText(student.getRiskNotes()),
                        formatTrainingRecords(recentTrainingRecords),
                        formatFitnessRecords(recentFitnessRecords)
                        ),
                fallback
        );
    }

    public GeneratedText generateAiGrowthDiscovery(String title, String evidence, String suggestedAction) {
        String fallback = defaultText(title) + "：" + defaultText(evidence) + "。建议：" + defaultText(suggestedAction);
        return generateWithFallbackResult(
                "你是青少年体能培训机构的 AI 成长分析助手。只基于用户提供的真实业务数据，输出一句简明、可执行的中文成长发现，不要编造姓名、班级、数字或历史趋势。",
                """
                        提醒标题：%s
                        真实证据：%s
                        建议动作：%s
                        """.formatted(
                        defaultText(title),
                        defaultText(evidence),
                        defaultText(suggestedAction)
                ),
                fallback
        );
    }

    public GeneratedText generateMonthlySchoolReport(String context, String fallback) {
        return generateWithFallbackResult(
                "你是青少年体能培训机构的 AI 运营分析助手。请基于真实数据生成中文全校月度分析报告，结构清楚，包含核心指标、成长发现、运营风险和下一步动作。不要编造未提供的数据。",
                context,
                fallback
        );
    }

    private String generateWithFallback(String systemPrompt, String userPrompt, String fallback) {
        return generateWithFallbackResult(systemPrompt, userPrompt, fallback).text();
    }

    private GeneratedText generateWithFallbackResult(String systemPrompt, String userPrompt, String fallback) {
        if (!properties.isEnabled()) {
            return new GeneratedText(fallback, false);
        }
        try {
            String generated = aiTextClient.complete(systemPrompt, userPrompt);
            if (StringUtils.hasText(generated)) {
                return new GeneratedText(generated.trim(), true);
            }
            return new GeneratedText(fallback, false);
        } catch (Exception ex) {
            return new GeneratedText(fallback, false);
        }
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "暂无";
    }

    private int safeSize(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private String formatTrainingRecords(List<TrainingRecord> records) {
        if (records == null || records.isEmpty()) {
            return "暂无";
        }
        return records.stream()
                .map(record -> formatDate(record.getTrainingDate())
                        + " " + defaultText(record.getTrainingContent())
                        + "；亮点：" + defaultText(record.getHighlightNote())
                        + "；待改进：" + defaultText(record.getImprovementNote())
                        + "；下次建议：" + defaultText(record.getNextStepSuggestion()))
                .reduce((left, right) -> left + "；" + right)
                .orElse("暂无");
    }

    private String formatFitnessRecords(List<FitnessTestRecord> records) {
        if (records == null || records.isEmpty()) {
            return "暂无";
        }
        return records.stream()
                .map(record -> formatDate(record.getTestDate())
                        + " " + defaultText(record.getItemName())
                        + " " + formatDecimal(record.getTestValue()) + defaultText(record.getUnit())
                        + "；备注：" + defaultText(record.getComment()))
                .reduce((left, right) -> left + "；" + right)
                .orElse("暂无");
    }

    private String formatDate(LocalDate date) {
        return date == null ? "未记录日期" : date.toString();
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
