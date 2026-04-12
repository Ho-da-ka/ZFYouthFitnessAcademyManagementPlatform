package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordCreateRequest;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingRecordServiceTest {

    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private StudentService studentService;
    @Mock
    private CourseService courseService;
    @Mock
    private GeneratedContentService generatedContentService;

    @InjectMocks
    private TrainingRecordService trainingRecordService;

    @Test
    void createShouldPersistStructuredFeedbackFields() {
        Student student = new Student();
        student.setStudentNo("S001");
        student.setName("Test Student");
        Course course = new Course();
        course.setCourseCode("C001");
        course.setName("Agility Training");

        when(studentService.getEntityById(1L)).thenReturn(student);
        when(courseService.getEntityById(2L)).thenReturn(course);
        when(trainingRecordMapper.insert(any(TrainingRecord.class))).thenReturn(1);
        when(generatedContentService.generateTrainingSummary(
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn("AI parent summary");

        TrainingRecordResponse response = trainingRecordService.create(new TrainingRecordCreateRequest(
                1L,
                2L,
                LocalDate.of(2026, 4, 15),
                "agility ladder + jump rope",
                60,
                "MEDIUM",
                "high completion",
                "stable foot cadence",
                "late-session stamina dropped",
                "do two stretching sets tonight",
                "reinforce hip stability next class",
                "better landing control than last week",
                null
        ));

        Assertions.assertEquals("stable foot cadence", response.highlightNote());
        Assertions.assertEquals("late-session stamina dropped", response.improvementNote());
        Assertions.assertEquals("do two stretching sets tonight", response.parentAction());
        Assertions.assertEquals("AI parent summary", response.aiSummary());
        Assertions.assertNull(response.parentReadAt());
    }
}
