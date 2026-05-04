package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.ai.AiHubOverviewResponse;
import com.shuzi.managementplatform.web.dto.ai.AiMonthlyReportResponse;
import com.shuzi.managementplatform.web.dto.ai.AiStudentInsightsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    private static final int DEFAULT_LIMIT = 500;
    private static final int ALERT_LIMIT = 3;

    private final StudentMapper studentMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StageEvaluationMapper stageEvaluationMapper;
    private final GeneratedContentService generatedContentService;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(
            StudentMapper studentMapper,
            TrainingRecordMapper trainingRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            StageEvaluationMapper stageEvaluationMapper,
            GeneratedContentService generatedContentService,
            ObjectMapper objectMapper
    ) {
        this.studentMapper = studentMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.stageEvaluationMapper = stageEvaluationMapper;
        this.generatedContentService = generatedContentService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AiStudentInsightsResponse getStudentInsights(Long studentId) {
        System.out.println("DEBUG: Entering getStudentInsights for student: " + studentId);
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + studentId);
        }

        if (StringUtils.hasText(student.getAiInsights())) {
            System.out.println("DEBUG: Found cached insights in DB for student: " + studentId);
            try {
                return objectMapper.readValue(student.getAiInsights(), AiStudentInsightsResponse.class);
            } catch (JsonProcessingException e) {
                System.out.println("DEBUG: Failed to parse cached insights: " + e.getMessage());
            }
        }
        
        System.out.println("DEBUG: No cached insights found for student: " + studentId + ", returning null");
        return null;
    }

    @Transactional
    public AiStudentInsightsResponse regenerateStudentInsights(Long studentId) {
        System.out.println("DEBUG: Manually regenerating insights for student: " + studentId);
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + studentId);
        }

        List<TrainingRecord> recentTrainingRecords = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getStudentId, studentId)
                        .orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId)
                        .last("limit 5")
        );
        List<FitnessTestRecord> recentFitnessRecords = fitnessTestRecordMapper.selectList(
                Wrappers.<FitnessTestRecord>lambdaQuery()
                        .eq(FitnessTestRecord::getStudentId, studentId)
                        .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId)
                        .last("limit 5")
        );

        String summary = generatedContentService.generateStudentInsightSummary(
                student,
                recentTrainingRecords,
                recentFitnessRecords
        );
        
        AiStudentInsightsResponse response = new AiStudentInsightsResponse(
                summary,
                buildStrengths(student, recentTrainingRecords, recentFitnessRecords),
                buildSuggestions(student, recentTrainingRecords)
        );

        try {
            student.setAiInsights(objectMapper.writeValueAsString(response));
            studentMapper.updateById(student);
        } catch (JsonProcessingException e) {
            // Log error
        }

        return response;
    }

    @Transactional(readOnly = true)
    public AiHubOverviewResponse getHubOverview() {
        return buildHubOverview(loadHubSnapshot());
    }

    @Transactional(readOnly = true)
    public AiMonthlyReportResponse generateMonthlyReport() {
        HubSnapshot snapshot = loadHubSnapshot();
        AiHubOverviewResponse overview = buildHubOverview(snapshot);
        String fallback = buildMonthlyFallback(overview);
        GeneratedContentService.GeneratedText generated = generatedContentService.generateMonthlySchoolReport(
                buildMonthlyContext(snapshot, overview),
                fallback
        );
        return new AiMonthlyReportResponse(LocalDateTime.now(), generated.text(), generated.generatedByAi());
    }

    private HubSnapshot loadHubSnapshot() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        List<Student> students = safeList(studentMapper.selectList(
                Wrappers.<Student>lambdaQuery()
                        .orderByDesc(Student::getId)
                        .last("limit " + DEFAULT_LIMIT)
        ));
        List<TrainingRecord> trainingRecords = safeList(trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .ge(TrainingRecord::getTrainingDate, monthStart)
                        .orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId)
                        .last("limit " + DEFAULT_LIMIT)
        ));
        List<FitnessTestRecord> fitnessRecords = safeList(fitnessTestRecordMapper.selectList(
                Wrappers.<FitnessTestRecord>lambdaQuery()
                        .ge(FitnessTestRecord::getTestDate, monthStart)
                        .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId)
                        .last("limit " + DEFAULT_LIMIT)
        ));
        List<AttendanceRecord> attendanceRecords = safeList(attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .ge(AttendanceRecord::getAttendanceDate, monthStart)
                        .orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
                        .last("limit " + DEFAULT_LIMIT)
        ));
        List<StageEvaluation> stageEvaluations = safeList(stageEvaluationMapper.selectList(
                Wrappers.<StageEvaluation>lambdaQuery()
                        .ge(StageEvaluation::getPeriodEnd, monthStart)
                        .orderByDesc(StageEvaluation::getPeriodEnd, StageEvaluation::getId)
                        .last("limit " + DEFAULT_LIMIT)
        ));
        return new HubSnapshot(today, monthStart, students, trainingRecords, fitnessRecords, attendanceRecords, stageEvaluations);
    }

    private AiHubOverviewResponse buildHubOverview(HubSnapshot snapshot) {
        Map<Long, Student> studentById = snapshot.students().stream()
                .filter(student -> student.getId() != null)
                .collect(Collectors.toMap(Student::getId, student -> student, (left, right) -> left));
        List<AiHubOverviewResponse.GrowthAlert> alerts = buildAlerts(snapshot, studentById);
        return new AiHubOverviewResponse(
                LocalDateTime.now(),
                buildMetrics(snapshot),
                alerts,
                buildRadarDimensions(snapshot.fitnessRecords())
        );
    }

    private List<AiHubOverviewResponse.Metric> buildMetrics(HubSnapshot snapshot) {
        LinkedHashSet<Long> activeStudentIds = new LinkedHashSet<>();
        snapshot.trainingRecords().forEach(record -> addIfPresent(activeStudentIds, record.getStudentId()));
        snapshot.fitnessRecords().forEach(record -> addIfPresent(activeStudentIds, record.getStudentId()));
        snapshot.attendanceRecords().forEach(record -> addIfPresent(activeStudentIds, record.getStudentId()));
        snapshot.stageEvaluations().forEach(record -> addIfPresent(activeStudentIds, record.getStudentId()));

        long aiSummaryCount = snapshot.trainingRecords().stream()
                .filter(record -> StringUtils.hasText(record.getAiSummary()))
                .count();
        long parentReportCount = snapshot.stageEvaluations().stream()
                .filter(record -> StringUtils.hasText(record.getParentReport()))
                .count();
        long interpretationCount = snapshot.stageEvaluations().stream()
                .filter(record -> StringUtils.hasText(record.getAiInterpretation()))
                .count();

        List<AiHubOverviewResponse.Metric> metrics = new ArrayList<>();
        metrics.add(new AiHubOverviewResponse.Metric(
                "activeStudents",
                "AI 活跃分析学员",
                compactNumber(activeStudentIds.size()),
                "位",
                "占总学员 " + percent(activeStudentIds.size(), snapshot.students().size()) + "%",
                "purple"
        ));
        metrics.add(new AiHubOverviewResponse.Metric(
                "summaryCount",
                "智能总结生成量",
                compactNumber(aiSummaryCount + interpretationCount + parentReportCount),
                "条",
                "本月真实生成内容统计",
                "blue"
        ));
        metrics.add(new AiHubOverviewResponse.Metric(
                "parentReports",
                "家长报告生成量",
                compactNumber(parentReportCount),
                "份",
                "来自阶段评估家长报告",
                "amber"
        ));
        return metrics;
    }

    private List<AiHubOverviewResponse.RadarDimension> buildRadarDimensions(List<FitnessTestRecord> fitnessRecords) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("爆发力", 0);
        counts.put("耐力", 0);
        counts.put("协调性", 0);
        counts.put("柔韧性", 0);
        counts.put("核心力量", 0);
        for (FitnessTestRecord record : fitnessRecords) {
            String dimension = classifyFitnessDimension(record.getItemName());
            counts.put(dimension, counts.getOrDefault(dimension, 0) + 1);
        }
        int maxCount = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return counts.entrySet().stream()
                .map(entry -> new AiHubOverviewResponse.RadarDimension(
                        entry.getKey(),
                        maxCount == 0 ? 0 : Math.round(entry.getValue() * 100f / maxCount)
                ))
                .toList();
    }

    private List<AiHubOverviewResponse.GrowthAlert> buildAlerts(
            HubSnapshot snapshot,
            Map<Long, Student> studentById
    ) {
        List<AlertCandidate> candidates = new ArrayList<>();
        candidates.addAll(buildTrainingCandidates(snapshot.trainingRecords(), studentById));
        candidates.addAll(buildAttendanceCandidates(snapshot.attendanceRecords(), studentById));
        candidates.addAll(buildStageEvaluationCandidates(snapshot.stageEvaluations(), studentById));

        if (candidates.isEmpty()) {
            return List.of(new AiHubOverviewResponse.GrowthAlert(
                    "data-gap",
                    "low",
                    "AI 数据不足",
                    "当前缺少本月训练、体测、考勤或阶段评估记录，暂时无法形成可靠成长发现。",
                    "先补充真实业务记录，再生成 AI 成长发现。",
                    "dashboard",
                    Map.of(),
                    false
            ));
        }

        List<AiHubOverviewResponse.GrowthAlert> alerts = new ArrayList<>();
        for (AlertCandidate candidate : candidates.stream().limit(ALERT_LIMIT).toList()) {
            alerts.add(new AiHubOverviewResponse.GrowthAlert(
                    candidate.id(),
                    candidate.type(),
                    candidate.title(),
                    candidate.fallbackDesc(),
                    candidate.suggestedAction(),
                    candidate.routeName(),
                    candidate.routeQuery(),
                    false
            ));
        }
        return alerts;
    }

    private List<AlertCandidate> buildTrainingCandidates(
            List<TrainingRecord> trainingRecords,
            Map<Long, Student> studentById
    ) {
        List<AlertCandidate> candidates = new ArrayList<>();
        for (TrainingRecord record : trainingRecords) {
            if (!hasAnyText(record.getHighlightNote(), record.getImprovementNote(), record.getNextStepSuggestion())) {
                continue;
            }
            Student student = studentById.get(record.getStudentId());
            String studentName = studentName(student, record.getStudentId());
            String title = "学员 " + studentName + " 训练表现需要跟进";
            String suggestedAction = "已为你打开学员管理并填入姓名，先核对学员档案与近期训练记录。";
            candidates.add(new AlertCandidate(
                    "training-" + identity(record.getId(), record.getStudentId()),
                    "high",
                    title,
                    trainingEvidence(studentName, record),
                    title + "：" + defaultText(record.getHighlightNote()) + "；建议：" + defaultText(record.getNextStepSuggestion()),
                    suggestedAction,
                    "students",
                    Map.of("name", studentName)
            ));
        }
        return candidates;
    }

    private List<AlertCandidate> buildAttendanceCandidates(
            List<AttendanceRecord> attendanceRecords,
            Map<Long, Student> studentById
    ) {
        Map<Long, int[]> statsByStudent = new HashMap<>();
        for (AttendanceRecord record : attendanceRecords) {
            if (record.getStudentId() == null) {
                continue;
            }
            int[] stats = statsByStudent.computeIfAbsent(record.getStudentId(), ignored -> new int[2]);
            stats[0]++;
            if (record.getStatus() == AttendanceStatus.PRESENT || record.getStatus() == AttendanceStatus.LATE) {
                stats[1]++;
            }
        }

        List<AlertCandidate> candidates = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : statsByStudent.entrySet()) {
            int total = entry.getValue()[0];
            int arrived = entry.getValue()[1];
            if (total < 2 || arrived * 100 >= total * 80) {
                continue;
            }
            Student student = studentById.get(entry.getKey());
            String studentName = studentName(student, entry.getKey());
            String title = "学员 " + studentName + " 出勤需要跟进";
            String evidence = "学员：" + studentName + "\n本月出勤记录：" + total + " 次，应到实到/迟到：" + arrived + " 次";
            String suggestedAction = "已为你打开学员管理并填入姓名，核对联系方式后联系家长确认原因。";
            candidates.add(new AlertCandidate(
                    "attendance-" + entry.getKey(),
                    "medium",
                    title,
                    evidence,
                    title + "：本月出勤率 " + percent(arrived, total) + "%，建议联系家长确认原因。",
                    suggestedAction,
                    "students",
                    Map.of("name", studentName)
            ));
        }
        return candidates;
    }

    private List<AlertCandidate> buildStageEvaluationCandidates(
            List<StageEvaluation> stageEvaluations,
            Map<Long, Student> studentById
    ) {
        List<AlertCandidate> candidates = new ArrayList<>();
        for (StageEvaluation evaluation : stageEvaluations) {
            if (!StringUtils.hasText(evaluation.getParentReport()) && !StringUtils.hasText(evaluation.getAiInterpretation())) {
                continue;
            }
            Student student = studentById.get(evaluation.getStudentId());
            String studentName = studentName(student, evaluation.getStudentId());
            String title = "学员 " + studentName + " 阶段评估已生成";
            String suggestedAction = "已为你打开阶段评估页面，复核 AI 解读与家长报告后再发布。";
            candidates.add(new AlertCandidate(
                    "stage-" + identity(evaluation.getId(), evaluation.getStudentId()),
                    "low",
                    title,
                    "学员：" + studentName + "\n周期：" + defaultText(evaluation.getCycleName())
                            + "\nAI 解读：" + defaultText(evaluation.getAiInterpretation())
                            + "\n家长报告：" + defaultText(evaluation.getParentReport()),
                    title + "，建议复核后统一发布。",
                    suggestedAction,
                    "stage-evaluations",
                    Map.of()
            ));
        }
        return candidates;
    }

    private String buildMonthlyFallback(AiHubOverviewResponse overview) {
        List<String> lines = new ArrayList<>();
        lines.add("全校月度分析报告");
        lines.add("生成时间：" + LocalDateTime.now());
        lines.add("");
        lines.add("核心指标");
        overview.metrics().forEach(metric -> lines.add(
                metric.label() + "：" + metric.value() + metric.unit() + "（" + metric.footer() + "）"
        ));
        lines.add("");
        lines.add("AI 实时成长发现");
        for (int i = 0; i < overview.alerts().size(); i++) {
            AiHubOverviewResponse.GrowthAlert alert = overview.alerts().get(i);
            lines.add((i + 1) + ". " + alert.title() + "：" + alert.desc());
        }
        lines.add("");
        lines.add("建议动作");
        for (int i = 0; i < overview.alerts().size(); i++) {
            lines.add((i + 1) + ". " + overview.alerts().get(i).suggestedAction());
        }
        return String.join("\n", lines);
    }

    private String buildMonthlyContext(HubSnapshot snapshot, AiHubOverviewResponse overview) {
        return """
                月份：%s 至 %s
                学员名单：%s
                核心指标：%s
                成长发现：%s
                近期训练样本：%s
                近期体测样本：%s
                近期阶段评估样本：%s
                """.formatted(
                snapshot.monthStart(),
                snapshot.today(),
                snapshot.students().stream().map(Student::getName).filter(StringUtils::hasText).limit(20).collect(Collectors.joining("、")),
                overview.metrics().stream().map(metric -> metric.label() + "=" + metric.value() + metric.unit() + "，" + metric.footer()).collect(Collectors.joining("；")),
                overview.alerts().stream().map(alert -> alert.title() + "：" + alert.desc()).collect(Collectors.joining("；")),
                snapshot.trainingRecords().stream().limit(8).map(this::trainingBrief).collect(Collectors.joining("；")),
                snapshot.fitnessRecords().stream().limit(8).map(this::fitnessBrief).collect(Collectors.joining("；")),
                snapshot.stageEvaluations().stream().limit(8).map(this::stageBrief).collect(Collectors.joining("；"))
        );
    }

    private List<AiStudentInsightsResponse.StrengthPoint> buildStrengths(
            Student student,
            List<TrainingRecord> recentTrainingRecords,
            List<FitnessTestRecord> recentFitnessRecords
    ) {
        List<AiStudentInsightsResponse.StrengthPoint> strengths = new ArrayList<>();
        if (StringUtils.hasText(student.getGoalFocus())) {
            strengths.add(new AiStudentInsightsResponse.StrengthPoint(
                    "目标方向",
                    "跟进中",
                    student.getGoalFocus().trim()
            ));
        }
        recentTrainingRecords.stream()
                .filter(record -> StringUtils.hasText(record.getHighlightNote()))
                .findFirst()
                .ifPresent(record -> strengths.add(new AiStudentInsightsResponse.StrengthPoint(
                        "课堂表现",
                        "近期反馈",
                        record.getHighlightNote().trim()
                )));
        recentFitnessRecords.stream()
                .filter(record -> StringUtils.hasText(record.getItemName()))
                .findFirst()
                .ifPresent(record -> strengths.add(new AiStudentInsightsResponse.StrengthPoint(
                        record.getItemName().trim(),
                        "最新体测",
                        formatFitnessValue(record)
                )));
        if (strengths.isEmpty()) {
            strengths.add(new AiStudentInsightsResponse.StrengthPoint(
                    "训练基础",
                    "待观察",
                    "暂无足够近期数据，建议先补充训练反馈和体测记录"
            ));
        }
        return strengths.stream().limit(3).toList();
    }

    private List<String> buildSuggestions(Student student, List<TrainingRecord> recentTrainingRecords) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        for (TrainingRecord record : recentTrainingRecords) {
            addIfPresent(suggestions, record.getImprovementNote());
            addIfPresent(suggestions, record.getNextStepSuggestion());
        }
        if (StringUtils.hasText(student.getRiskNotes())) {
            suggestions.add("关注：" + student.getRiskNotes().trim());
        }
        if (suggestions.isEmpty()) {
            suggestions.add("补充近期训练记录和体测数据，便于生成更准确的成长分析");
        }
        return suggestions.stream().limit(3).toList();
    }

    private void addIfPresent(LinkedHashSet<String> target, String text) {
        if (StringUtils.hasText(text)) {
            target.add(text.trim());
        }
    }

    private void addIfPresent(LinkedHashSet<Long> target, Long value) {
        if (value != null) {
            target.add(value);
        }
    }

    private String trainingEvidence(String studentName, TrainingRecord record) {
        return "学员：" + studentName
                + "\n训练日期：" + formatDate(record.getTrainingDate())
                + "\n训练内容：" + defaultText(record.getTrainingContent())
                + "\n课堂亮点：" + defaultText(record.getHighlightNote())
                + "\n待改进：" + defaultText(record.getImprovementNote())
                + "\n下次建议：" + defaultText(record.getNextStepSuggestion());
    }

    private String trainingBrief(TrainingRecord record) {
        return formatDate(record.getTrainingDate()) + " 学员#" + defaultText(record.getStudentId())
                + " " + defaultText(record.getTrainingContent())
                + "，亮点：" + defaultText(record.getHighlightNote())
                + "，建议：" + defaultText(record.getNextStepSuggestion());
    }

    private String fitnessBrief(FitnessTestRecord record) {
        return formatDate(record.getTestDate()) + " 学员#" + defaultText(record.getStudentId())
                + " " + defaultText(record.getItemName()) + " " + formatFitnessValue(record);
    }

    private String stageBrief(StageEvaluation evaluation) {
        return defaultText(evaluation.getCycleName()) + " 学员#" + defaultText(evaluation.getStudentId())
                + "，AI解读：" + defaultText(evaluation.getAiInterpretation())
                + "，家长报告：" + defaultText(evaluation.getParentReport());
    }

    private String formatFitnessValue(FitnessTestRecord record) {
        String value = record.getTestValue() == null
                ? "暂无数值"
                : record.getTestValue().stripTrailingZeros().toPlainString();
        String unit = StringUtils.hasText(record.getUnit()) ? record.getUnit().trim() : "";
        return value + unit;
    }

    private String compactNumber(long value) {
        if (value >= 1000) {
            String text = String.format(Locale.ROOT, "%.1fk", value / 1000.0);
            return text.replace(".0k", "k");
        }
        return Long.toString(value);
    }

    private int percent(long part, long total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round(part * 100f / total);
    }

    private String studentName(Student student, Long studentId) {
        if (student != null && StringUtils.hasText(student.getName())) {
            return student.getName().trim();
        }
        return studentId == null ? "未知学员" : "学员#" + studentId;
    }

    private boolean hasAnyText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return true;
            }
        }
        return false;
    }

    private String classifyFitnessDimension(String itemName) {
        String text = StringUtils.hasText(itemName) ? itemName : "";
        if (text.contains("跳") || text.contains("爆发")) {
            return "爆发力";
        }
        if (text.contains("耐") || text.contains("跑")) {
            return "耐力";
        }
        if (text.contains("协调") || text.contains("敏捷")) {
            return "协调性";
        }
        if (text.contains("柔") || text.contains("体前屈")) {
            return "柔韧性";
        }
        return "核心力量";
    }

    private String identity(Long id, Long fallbackId) {
        if (id != null) {
            return id.toString();
        }
        return fallbackId == null ? "unknown" : fallbackId.toString();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "未记录日期" : date.toString();
    }

    private String defaultText(Object value) {
        if (value == null) {
            return "暂无";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text.trim() : "暂无";
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record HubSnapshot(
            LocalDate today,
            LocalDate monthStart,
            List<Student> students,
            List<TrainingRecord> trainingRecords,
            List<FitnessTestRecord> fitnessRecords,
            List<AttendanceRecord> attendanceRecords,
            List<StageEvaluation> stageEvaluations
    ) {}

    private record AlertCandidate(
            String id,
            String type,
            String title,
            String evidence,
            String fallbackDesc,
            String suggestedAction,
            String routeName,
            Map<String, String> routeQuery
    ) {}
}
