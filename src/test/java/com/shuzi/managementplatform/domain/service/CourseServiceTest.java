package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.config.CourseConflictChecker;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.course.CourseCreateRequest;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseMapper courseMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private StudentService studentService;
    @Mock
    private CourseConflictChecker courseConflictChecker;

    @InjectMocks
    private CourseService courseService;

    private Course course;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setCourseCode("C001");
        course.setName("课程A");
    }

    @Test
    void deleteShouldThrowWhenCourseNotFound() {
        when(courseMapper.selectById(1L)).thenReturn(null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> courseService.delete(1L, false));
    }

    @Test
    void deleteShouldThrowWhenReferencedAndNotForce() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(2L);
        when(trainingRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(1L);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> courseService.delete(1L, false));
        Assertions.assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(courseMapper, never()).deleteById(1L);
    }

    @Test
    void deleteForceShouldCascadeAndDeleteCourse() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(2L);
        when(trainingRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(1L);

        courseService.delete(1L, true);

        verify(attendanceRecordMapper, times(1)).delete(ArgumentMatchers.any());
        verify(trainingRecordMapper, times(1)).delete(ArgumentMatchers.any());
        verify(courseMapper, times(1)).deleteById(1L);
    }

    @Test
    void createShouldExposeTrainingThemeFields() {
        CourseCreateRequest request = new CourseCreateRequest(
                "C1001",
                "Agility Basics",
                "GROUP",
                "Coach Wang",
                "Hall A",
                LocalDateTime.of(2026, 4, 20, 18, 0),
                60,
                12,
                LocalDate.of(2026, 4, 20),
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                CourseStatus.PLANNED,
                "basic coordination course",
                "agility theme",
                "7-9",
                "coordination,core",
                "ladder footwork,landing stability"
        );

        when(courseMapper.selectCount(ArgumentMatchers.any())).thenReturn(0L);
        when(courseConflictChecker.findConflicts(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.isNull()
        )).thenReturn(List.of());

        CourseResponse response = courseService.create(request, false);

        Assertions.assertEquals("agility theme", response.trainingTheme());
        Assertions.assertEquals("7-9", response.targetAgeRange());
        Assertions.assertEquals("ladder footwork,landing stability", response.focusPoints());
    }
}

