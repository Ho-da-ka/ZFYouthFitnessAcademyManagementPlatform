package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationCreateRequest;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageEvaluationServiceTest {

    @Mock
    private StageEvaluationMapper stageEvaluationMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private StudentService studentService;
    @Mock
    private GeneratedContentService generatedContentService;
    @Mock
    private CareAlertService careAlertService;

    @InjectMocks
    private StageEvaluationService stageEvaluationService;

    @Test
    void createShouldCalculateAttendanceRateAndPersistNarrative() {
        Student student = new Student();
        student.setName("Test Student");
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(attendanceRecordMapper.selectCount(any())).thenReturn(8L, 6L);
        when(stageEvaluationMapper.insert(any(StageEvaluation.class))).thenReturn(1);
        when(generatedContentService.generateStageInterpretation(any(), any(), any(), any()))
                .thenReturn("AI interpretation");
        when(generatedContentService.generateParentReport(any(), any(), anyDouble(), any(), any(), any()))
                .thenReturn("Parent report");

        StageEvaluationResponse response = stageEvaluationService.create(new StageEvaluationCreateRequest(
                1L,
                "2026 Spring Cycle 1",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                "5/6 sessions completed",
                "50m sprint improved by 0.4s",
                "core stability improved",
                "next phase focuses on lower-body power"
        ));

        Assertions.assertEquals(0.75, response.attendanceRate());
        Assertions.assertEquals("50m sprint improved by 0.4s", response.fitnessSummary());
        Assertions.assertEquals("next phase focuses on lower-body power", response.nextStagePlan());
        Assertions.assertEquals("AI interpretation", response.aiInterpretation());
        Assertions.assertEquals("Parent report", response.parentReport());
        org.mockito.Mockito.verify(careAlertService).refreshStudentAlerts(1L);
    }
}
