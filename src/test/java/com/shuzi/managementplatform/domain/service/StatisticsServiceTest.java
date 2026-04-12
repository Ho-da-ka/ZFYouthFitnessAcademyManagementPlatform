package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.statistics.DashboardStatsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StudentMapper studentMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CoachMapper coachMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private TrainingRecordMapper trainingRecordMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getDashboardShouldReturnNonNullStructure() {
        when(studentMapper.selectCount(ArgumentMatchers.any())).thenReturn(10L, 8L);
        when(courseMapper.selectCount(ArgumentMatchers.any())).thenReturn(6L, 1L, 2L, 2L, 1L);
        when(coachMapper.selectCount(ArgumentMatchers.any())).thenReturn(4L, 3L);
        when(attendanceRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(20L, 18L, 0L);
        when(fitnessTestRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(5L);
        when(trainingRecordMapper.selectCount(ArgumentMatchers.any())).thenReturn(7L);

        DashboardStatsResponse response = statisticsService.getDashboard();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.students());
        Assertions.assertNotNull(response.courses());
        Assertions.assertNotNull(response.coaches());
        Assertions.assertNotNull(response.attendance());
    }

}
