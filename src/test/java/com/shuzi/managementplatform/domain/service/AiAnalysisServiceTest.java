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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getStudentInsightsShouldReturnNullIfNoCachedInsights() {
        Student student = new Student();
        student.setName("阿斯蒂芬");
        
        when(studentMapper.selectById(123L)).thenReturn(student);

        AiAnalysisService service = new AiAnalysisService(
                studentMapper,
                trainingRecordMapper,
                fitnessTestRecordMapper,
                attendanceRecordMapper,
                stageEvaluationMapper,
                generatedContentService,
                objectMapper
        );

        AiStudentInsightsResponse response = service.getStudentInsights(123L);

        Assertions.assertNull(response, "Should return null when ai_insights column is empty in DB");
    }

    @Test
    void getStudentInsightsShouldReturnCachedInsightsIfPresent() throws Exception {
        Student student = new Student();
        student.setName("阿斯蒂芬");
        AiStudentInsightsResponse cached = new AiStudentInsightsResponse("Cached result", List.of(), List.of());
        student.setAiInsights(objectMapper.writeValueAsString(cached));

        when(studentMapper.selectById(123L)).thenReturn(student);

        AiAnalysisService service = new AiAnalysisService(
                studentMapper,
                trainingRecordMapper,
                fitnessTestRecordMapper,
                attendanceRecordMapper,
                stageEvaluationMapper,
                generatedContentService,
                objectMapper
        );

        AiStudentInsightsResponse response = service.getStudentInsights(123L);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Cached result", response.summary());
    }

    @Test
    void regenerateStudentInsightsShouldUpdateDatabase() throws Exception {
        Student student = new Student();
        ReflectionTestUtils.setField(student, "id", 123L);
        student.setName("阿斯蒂芬");

        when(studentMapper.selectById(123L)).thenReturn(student);
        when(trainingRecordMapper.selectList(any())).thenReturn(List.of());
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of());
        when(generatedContentService.generateStudentInsightSummary(any(), any(), any())).thenReturn("New result");

        AiAnalysisService service = new AiAnalysisService(
                studentMapper,
                trainingRecordMapper,
                fitnessTestRecordMapper,
                attendanceRecordMapper,
                stageEvaluationMapper,
                generatedContentService,
                objectMapper
        );

        AiStudentInsightsResponse response = service.regenerateStudentInsights(123L);

        Assertions.assertEquals("New result", response.summary());
        verify(studentMapper).updateById(any(Student.class));
        Assertions.assertTrue(student.getAiInsights().contains("New result"));
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
                generatedContentService,
                objectMapper
        );
    }
}
