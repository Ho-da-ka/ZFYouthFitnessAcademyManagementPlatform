package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceBatchCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceUpdateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private StudentService studentService;
    @Mock
    private CourseService courseService;
    @Mock
    private CareAlertService careAlertService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setStudentNo("S001");
        student.setName("测试学员");
        course = new Course();
        course.setCourseCode("C001");
        course.setName("测试课程");
        course.setMaxCapacity(null);
    }

    @Test
    void createShouldThrowWhenDuplicateRecordExists() {
        AttendanceCreateRequest request = new AttendanceCreateRequest(
                1L, 2L, LocalDate.now(), AttendanceStatus.PRESENT, "dup");
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(1L);

        Assertions.assertThrows(BusinessException.class, () -> attendanceService.create(request));
        verify(attendanceRecordMapper, never()).insert(ArgumentMatchers.any(AttendanceRecord.class));
    }

    @Test
    void batchCreateShouldSkipDuplicateRows() {
        AttendanceBatchCreateRequest request = new AttendanceBatchCreateRequest(
                2L, LocalDate.now(), List.of(1L, 2L, 2L), AttendanceStatus.PRESENT);
        when(courseService.getEntityById(2L)).thenReturn(course);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(studentService.getEntityById(2L)).thenReturn(student);
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(0L, 1L);

        List<?> responses = attendanceService.batchCreate(request);

        Assertions.assertEquals(1, responses.size());
        verify(attendanceRecordMapper, times(1)).insert(ArgumentMatchers.any(AttendanceRecord.class));
    }

    @Test
    void createShouldRefreshCareAlertsForStudent() {
        AttendanceCreateRequest request = new AttendanceCreateRequest(
                1L, 2L, LocalDate.now(), AttendanceStatus.ABSENT, "chain absence");
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(0L, 0L);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(courseService.getEntityById(2L)).thenReturn(course);

        attendanceService.create(request);

        verify(careAlertService).refreshStudentAlerts(1L);
    }

    @Test
    void updateShouldPersistStatusAndNote() {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(1L);
        record.setCourseId(2L);
        record.setAttendanceDate(LocalDate.now());
        record.setStatus(AttendanceStatus.ABSENT);
        when(attendanceRecordMapper.selectById(10L)).thenReturn(record);
        when(studentService.getEntityById(1L)).thenReturn(student);
        when(courseService.getEntityById(2L)).thenReturn(course);

        attendanceService.update(10L, new AttendanceUpdateRequest(AttendanceStatus.LATE, "补签"));

        Assertions.assertEquals(AttendanceStatus.LATE, record.getStatus());
        Assertions.assertEquals("补签", record.getNote());
        verify(attendanceRecordMapper, times(1)).updateById(record);
        verify(careAlertService).refreshStudentAlerts(1L);
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(attendanceRecordMapper.selectById(99L)).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> attendanceService.delete(99L));
    }
}
