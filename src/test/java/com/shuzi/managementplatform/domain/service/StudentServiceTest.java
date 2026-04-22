package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentMapper studentMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private UserAccountService userAccountService;
    @Mock
    private ParentAccountSyncService parentAccountSyncService;

    @InjectMocks
    private StudentService studentService;

    @Test
    void createShouldExposeGoalFieldsInResponse() {
        when(studentMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            ReflectionTestUtils.setField(student, "id", 101L);
            return 1;
        }).when(studentMapper).insert(any(Student.class));

        StudentResponse response = studentService.create(new StudentCreateRequest(
                "S1001",
                "Li Lei",
                Gender.MALE,
                LocalDate.of(2016, 5, 12),
                "Parent Li",
                "13800138000",
                StudentStatus.ACTIVE,
                "base class",
                "coordination improvement",
                "core stability,flexibility",
                "sensitive knee",
                LocalDate.of(2026, 4, 14),
                LocalDate.of(2026, 5, 12)
        ));

        Assertions.assertEquals("coordination improvement", response.goalFocus());
        Assertions.assertEquals("core stability,flexibility", response.trainingTags());
        Assertions.assertEquals(LocalDate.of(2026, 5, 12), response.goalEndDate());
        verify(parentAccountSyncService).syncStudentGuardianBinding(any(Student.class), any());
    }

    @Test
    void updateShouldSyncParentBindingWithPreviousGuardianPhone() {
        Student existing = new Student();
        ReflectionTestUtils.setField(existing, "id", 9L);
        existing.setStudentNo("S1009");
        existing.setGuardianPhone("13700137000");

        when(studentMapper.selectById(9L)).thenReturn(existing);

        studentService.update(9L, new StudentUpdateRequest(
                "Wang Xiao",
                Gender.FEMALE,
                LocalDate.of(2015, 8, 5),
                "Parent Wang",
                "13900139000",
                StudentStatus.ACTIVE,
                "updated",
                "power",
                "coordination",
                "none",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 1)
        ));

        verify(parentAccountSyncService).syncStudentGuardianBinding(existing, "13700137000");
    }
}
