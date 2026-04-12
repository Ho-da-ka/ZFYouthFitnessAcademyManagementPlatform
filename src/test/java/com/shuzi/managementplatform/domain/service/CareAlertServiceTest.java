package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.CareAlert;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CareAlertMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareAlertServiceTest {

    @Mock
    private CareAlertMapper careAlertMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private StageEvaluationMapper stageEvaluationMapper;
    @Mock
    private StudentService studentService;

    @InjectMocks
    private CareAlertService careAlertService;

    @Test
    void refreshStudentAlertsShouldCreateAbsenceStreakAlert() {
        Student student = new Student();
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(careAlertMapper.selectList(any())).thenReturn(List.of());
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of(
                attendance(LocalDate.of(2026, 4, 10), AttendanceStatus.ABSENT),
                attendance(LocalDate.of(2026, 4, 8), AttendanceStatus.ABSENT)
        ));
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of());

        careAlertService.refreshStudentAlerts(1L);

        verify(careAlertMapper).insert(org.mockito.ArgumentMatchers.<CareAlert>argThat(alert ->
                "ABSENCE_STREAK".equals(alert.getAlertType())
                        && "OPEN".equals(alert.getStatus())
                        && alert.getAlertTitle().contains("缺勤")));
    }

    @Test
    void refreshStudentAlertsShouldCreateFitnessRegressionAlert() {
        Student student = new Student();
        when(studentService.getEntityById(2L)).thenReturn(student);
        when(careAlertMapper.selectList(any())).thenReturn(List.of());
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of());
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of(
                fitness(LocalDate.of(2026, 4, 12), "50米跑", "秒", "9.20"),
                fitness(LocalDate.of(2026, 4, 5), "50米跑", "秒", "8.90")
        ));

        careAlertService.refreshStudentAlerts(2L);

        verify(careAlertMapper).insert(org.mockito.ArgumentMatchers.<CareAlert>argThat(alert ->
                "FITNESS_REGRESSION".equals(alert.getAlertType())
                        && alert.getAlertContent().contains("体测")));
    }

    @Test
    void refreshStudentAlertsShouldResolveOpenAlertWhenConditionClears() {
        Student student = new Student();
        when(studentService.getEntityById(3L)).thenReturn(student);

        CareAlert alert = new CareAlert();
        alert.setId(99L);
        alert.setStudentId(3L);
        alert.setAlertType("ABSENCE_STREAK");
        alert.setStatus("OPEN");

        when(careAlertMapper.selectList(any())).thenReturn(List.of(alert));
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of(
                attendance(LocalDate.of(2026, 4, 12), AttendanceStatus.PRESENT)
        ));
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of());

        careAlertService.refreshStudentAlerts(3L);

        verify(careAlertMapper).updateById(org.mockito.ArgumentMatchers.<CareAlert>argThat(updated ->
                Long.valueOf(99L).equals(updated.getId())
                        && "RESOLVED".equals(updated.getStatus())
                        && updated.getResolvedAt() != null));
    }

    @Test
    void refreshStudentAlertsShouldCreateOverdueEvaluationAlert() {
        Student student = new Student();
        student.setGoalEndDate(LocalDate.of(2026, 4, 1));
        when(studentService.getEntityById(4L)).thenReturn(student);
        when(careAlertMapper.selectList(any())).thenReturn(List.of());
        when(attendanceRecordMapper.selectList(any())).thenReturn(List.of());
        when(fitnessTestRecordMapper.selectList(any())).thenReturn(List.of());
        when(stageEvaluationMapper.selectCount(any())).thenReturn(0L);

        careAlertService.refreshStudentAlerts(4L);

        verify(careAlertMapper).insert(org.mockito.ArgumentMatchers.<CareAlert>argThat(alert ->
                "EVALUATION_OVERDUE".equals(alert.getAlertType())
                        && alert.getAlertTitle().contains("评估")));
    }

    private AttendanceRecord attendance(LocalDate date, AttendanceStatus status) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceDate(date);
        record.setStatus(status);
        return record;
    }

    private FitnessTestRecord fitness(LocalDate date, String itemName, String unit, String value) {
        FitnessTestRecord record = new FitnessTestRecord();
        record.setTestDate(date);
        record.setItemName(itemName);
        record.setUnit(unit);
        record.setTestValue(new BigDecimal(value));
        return record;
    }
}
