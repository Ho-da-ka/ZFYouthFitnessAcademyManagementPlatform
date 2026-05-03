package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.ai.AiHubOverviewResponse;
import com.shuzi.managementplatform.web.dto.ai.AiMonthlyReportResponse;
import com.shuzi.managementplatform.web.dto.ai.AiStudentInsightsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private TrainingRecordMapper trainingRecordMapper;

    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    @Mock
    private StageEvaluationMapper stageEvaluationMapper;

    @Mock
    private GeneratedContentService generatedContentService;

    @Test
    void getStudentInsightsShouldUseGeneratedContentAndRecentStudentData() {
        Student student = new Student();
        student.setName("阿斯蒂芬");
        student.setGoalFocus("爆发力提升");

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setTrainingDate(LocalDate.of(2026, 4, 6));
        trainingRecord.setTrainingContent("立定跳远专项");
        trainingRecord.setHighlightNote("起跳稳定");
        trainingRecord.setImprovementNote("核心耐力不足");
        trainingRecord.setNextStepSuggestion("每周增加一次核心稳定训练");

        FitnessTestRecord fitnessTestRecord = new FitnessTestRecord();
        fitnessTestRecord.setTestDate(LocalDate.of(2026, 4, 6));
        fitnessTestRecord.setItemName("立定跳远");
        fitnessTestRecord.setTestValue(BigDecimal.valueOf(185));
        fitnessTestRecord.setUnit("cm");

        when(studentMapper.selectById(123L)).thenReturn(student);
        when(trainingRecordMapper.selectList(any())).thenReturn(List.of(trainingRecord));
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of(fitnessTestRecord));
        when(generatedContentService.generateStudentInsightSummary(
                eq(student),
                eq(List.of(trainingRecord)),
                eq(List.of(fitnessTestRecord))
        )).thenReturn("DeepSeek 认为爆发力进步明显");

        AiAnalysisService service = new AiAnalysisService(
                studentMapper,
                trainingRecordMapper,
                fitnessTestRecordMapper,
                attendanceRecordMapper,
                stageEvaluationMapper,
                generatedContentService
        );

        AiStudentInsightsResponse response = service.getStudentInsights(123L);

        Assertions.assertEquals("DeepSeek 认为爆发力进步明显", response.summary());
        Assertions.assertTrue(response.strengths().stream()
                .anyMatch(point -> "课堂表现".equals(point.item()) && point.description().contains("起跳稳定")));
        Assertions.assertTrue(response.strengths().stream()
                .anyMatch(point -> "立定跳远".equals(point.item()) && point.description().contains("185cm")));
        Assertions.assertTrue(response.suggestions().contains("核心耐力不足"));
        Assertions.assertTrue(response.suggestions().contains("每周增加一次核心稳定训练"));

        verify(generatedContentService).generateStudentInsightSummary(
                student,
                List.of(trainingRecord),
                List.of(fitnessTestRecord)
        );
    }

    @Test
    void getHubOverviewShouldUseRealDataWithoutCallingDeepSeek() {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 11L);
        student.setName("王小明");
        student.setGoalFocus("爆发力提升");

        TrainingRecord trainingRecord = new TrainingRecord();
        ReflectionTestUtils.setField(trainingRecord, "id", 77L);
        trainingRecord.setStudentId(11L);
        trainingRecord.setTrainingDate(LocalDate.of(2026, 5, 2));
        trainingRecord.setTrainingContent("立定跳远专项");
        trainingRecord.setHighlightNote("起跳速度提升");
        trainingRecord.setImprovementNote("落地稳定性仍需加强");
        trainingRecord.setNextStepSuggestion("更新下一阶段训练计划");
        trainingRecord.setAiSummary("本次训练总结已生成");

        FitnessTestRecord fitnessTestRecord = new FitnessTestRecord();
        fitnessTestRecord.setStudentId(11L);
        fitnessTestRecord.setTestDate(LocalDate.of(2026, 5, 2));
        fitnessTestRecord.setItemName("立定跳远");
        fitnessTestRecord.setTestValue(BigDecimal.valueOf(190));
        fitnessTestRecord.setUnit("cm");

        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setStudentId(11L);
        attendanceRecord.setAttendanceDate(LocalDate.of(2026, 5, 2));
        attendanceRecord.setStatus(AttendanceStatus.PRESENT);

        StageEvaluation evaluation = new StageEvaluation();
        evaluation.setStudentId(11L);
        evaluation.setPeriodEnd(LocalDate.of(2026, 5, 2));
        evaluation.setParentReport("家长报告已生成");

        when(studentMapper.selectList(any())).thenReturn(List.of(student));
        when(trainingRecordMapper.selectList(any())).thenReturn(List.of(trainingRecord));
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of(fitnessTestRecord));
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of(attendanceRecord));
        when(stageEvaluationMapper.selectList(any())).thenReturn(List.of(evaluation));
        AiAnalysisService service = newService();

        AiHubOverviewResponse response = service.getHubOverview();

        Assertions.assertTrue(response.metrics().stream()
                .anyMatch(metric -> "activeStudents".equals(metric.key()) && "1".equals(metric.value())));
        AiHubOverviewResponse.GrowthAlert alert = response.alerts().get(0);
        Assertions.assertEquals("students", alert.routeName());
        Assertions.assertEquals(Map.of("name", "王小明"), alert.routeQuery());
        Assertions.assertTrue(alert.desc().contains("起跳速度提升"));
        Assertions.assertFalse(alert.generatedByAi());
        Assertions.assertTrue(response.radarDimensions().stream()
                .anyMatch(dimension -> "爆发力".equals(dimension.name()) && dimension.value() > 0));

        verify(generatedContentService, never())
                .generateAiGrowthDiscovery(anyString(), anyString(), anyString());
    }

    @Test
    void generateMonthlyReportShouldCallDeepSeekWithHubContext() {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 11L);
        student.setName("王小明");

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setStudentId(11L);
        trainingRecord.setTrainingDate(LocalDate.of(2026, 5, 2));
        trainingRecord.setTrainingContent("立定跳远专项");
        trainingRecord.setHighlightNote("起跳速度提升");

        when(studentMapper.selectList(any())).thenReturn(List.of(student));
        when(trainingRecordMapper.selectList(any())).thenReturn(List.of(trainingRecord));
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of());
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of());
        when(stageEvaluationMapper.selectList(any())).thenReturn(List.of());
        when(generatedContentService.generateMonthlySchoolReport(anyString(), anyString()))
                .thenReturn(new GeneratedContentService.GeneratedText("DeepSeek 月报内容", true));

        AiAnalysisService service = newService();

        AiMonthlyReportResponse response = service.generateMonthlyReport();

        Assertions.assertEquals("DeepSeek 月报内容", response.report());
        Assertions.assertTrue(response.generatedByAi());
        verify(generatedContentService).generateMonthlySchoolReport(
                org.mockito.ArgumentMatchers.contains("王小明"),
                anyString()
        );
        verify(generatedContentService, never())
                .generateAiGrowthDiscovery(anyString(), anyString(), anyString());
    }

    private AiAnalysisService newService() {
        return new AiAnalysisService(
                studentMapper,
                trainingRecordMapper,
                fitnessTestRecordMapper,
                attendanceRecordMapper,
                stageEvaluationMapper,
                generatedContentService
        );
    }
}
