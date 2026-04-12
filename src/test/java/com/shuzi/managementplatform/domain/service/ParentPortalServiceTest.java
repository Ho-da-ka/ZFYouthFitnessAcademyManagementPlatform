package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.ParentAccount;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.CourseBooking;
import com.shuzi.managementplatform.domain.entity.StageEvaluation;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseBookingMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.InAppMessageMapper;
import com.shuzi.managementplatform.domain.mapper.ParentAccountMapper;
import com.shuzi.managementplatform.domain.mapper.ParentStudentRelationMapper;
import com.shuzi.managementplatform.domain.mapper.StageEvaluationMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinCreateRequest;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentGrowthOverviewResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentPortalServiceTest {

    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private ParentAccountMapper parentAccountMapper;
    @Mock
    private ParentStudentRelationMapper parentStudentRelationMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseBookingMapper courseBookingMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private FitnessTestRecordMapper fitnessTestRecordMapper;
    @Mock
    private InAppMessageMapper inAppMessageMapper;
    @Mock
    private TrainingRecordMapper trainingRecordMapper;
    @Mock
    private StageEvaluationMapper stageEvaluationMapper;
    @Mock
    private CareAlertService careAlertService;

    @InjectMocks
    private ParentPortalService parentPortalService;

    @Test
    void getGrowthOverviewShouldAggregateGoalFeedbackAndEvaluation() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername("parent");
        userAccount.setRole("PARENT");
        userAccount.setStatus("ACTIVE");

        ParentAccount parentAccount = new ParentAccount();

        Student student = new Student();
        student.setName("Test Student");
        student.setGoalFocus("coordination improvement");
        student.setTrainingTags("core stability,flexibility");
        student.setRiskNotes("sensitive knee");
        student.setGoalStartDate(LocalDate.of(2026, 4, 1));
        student.setGoalEndDate(LocalDate.of(2026, 4, 30));

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setTrainingDate(LocalDate.of(2026, 4, 15));
        trainingRecord.setTrainingContent("agility ladder + jump rope");
        trainingRecord.setHighlightNote("stable foot cadence");
        trainingRecord.setImprovementNote("late-session stamina dropped");
        trainingRecord.setParentAction("do two stretching sets tonight");

        StageEvaluation evaluation = new StageEvaluation();
        evaluation.setCycleName("2026 Spring Cycle 1");
        evaluation.setAttendanceRate(java.math.BigDecimal.valueOf(0.75));
        evaluation.setFitnessSummary("50m sprint improved by 0.4s");
        evaluation.setCoachEvaluation("core stability improved");
        evaluation.setNextStagePlan("next phase focuses on lower-body power");

        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(parentAccountMapper.selectOne(any())).thenReturn(parentAccount);
        when(parentStudentRelationMapper.selectCount(any())).thenReturn(1L);
        when(studentMapper.selectById(1L)).thenReturn(student);
        when(trainingRecordMapper.selectList(any())).thenReturn(List.of(trainingRecord));
        when(stageEvaluationMapper.selectOne(any())).thenReturn(evaluation);

        ParentGrowthOverviewResponse overview = parentPortalService.getGrowthOverview("parent", 1L);

        Assertions.assertEquals("coordination improvement", overview.goalFocus());
        Assertions.assertEquals(1, overview.recentTrainingFeedback().size());
        Assertions.assertNotNull(overview.latestEvaluation());
    }

    @Test
    void createCheckinShouldRefreshCareAlerts() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername("parent");
        userAccount.setRole("PARENT");
        userAccount.setStatus("ACTIVE");

        ParentAccount parentAccount = new ParentAccount();
        ReflectionTestUtils.setField(parentAccount, "id", 9L);

        CourseBooking booking = new CourseBooking();
        booking.setParentAccountId(9L);
        booking.setStudentId(1L);
        booking.setCourseId(2L);
        booking.setBookingStatus("BOOKED");

        Student student = new Student();
        student.setName("Test Student");

        Course course = new Course();
        course.setName("Agility Training");

        when(userAccountMapper.selectOne(any())).thenReturn(userAccount);
        when(parentAccountMapper.selectOne(any())).thenReturn(parentAccount);
        when(courseBookingMapper.selectOne(any())).thenReturn(booking);
        when(attendanceRecordMapper.selectCount(any())).thenReturn(0L);
        when(studentMapper.selectById(1L)).thenReturn(student);
        when(courseMapper.selectById(2L)).thenReturn(course);

        ParentCheckinResponse response = parentPortalService.createCheckin(
                "parent",
                new ParentCheckinCreateRequest(6L, LocalDate.of(2026, 4, 20), "checked in")
        );

        Assertions.assertEquals(AttendanceStatus.PRESENT, response.status());
        verify(careAlertService).refreshStudentAlerts(1L);
    }
}
