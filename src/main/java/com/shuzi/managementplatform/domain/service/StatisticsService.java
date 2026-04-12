package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Coach;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.statistics.CoachWorkloadResponse;
import com.shuzi.managementplatform.web.dto.statistics.DashboardStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final TrainingRecordMapper trainingRecordMapper;

    public StatisticsService(StudentMapper studentMapper, CourseMapper courseMapper,
                             CoachMapper coachMapper, AttendanceRecordMapper attendanceRecordMapper,
                             FitnessTestRecordMapper fitnessTestRecordMapper,
                             TrainingRecordMapper trainingRecordMapper) {
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.coachMapper = coachMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.trainingRecordMapper = trainingRecordMapper;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboard() {
        // Students
        long studentTotal = studentMapper.selectCount(null);
        long studentActive = studentMapper.selectCount(
                Wrappers.<Student>lambdaQuery().eq(Student::getStatus, StudentStatus.ACTIVE));
        long studentInactive = studentTotal - studentActive;

        // Courses
        long courseTotal = courseMapper.selectCount(null);
        long coursePlanned = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getStatus, CourseStatus.PLANNED));
        long courseOngoing = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getStatus, CourseStatus.ONGOING));
        long courseCompleted = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getStatus, CourseStatus.COMPLETED));
        long courseCancelled = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getStatus, CourseStatus.CANCELLED));

        // Coaches
        long coachTotal = coachMapper.selectCount(null);
        long coachActive = coachMapper.selectCount(
                Wrappers.<Coach>lambdaQuery().eq(Coach::getStatus, CoachStatus.ACTIVE));
        long coachInactive = coachTotal - coachActive;

        // Attendance this month
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        long thisMonthTotal = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .ge(AttendanceRecord::getAttendanceDate, monthStart)
                        .le(AttendanceRecord::getAttendanceDate, today));
        long thisMonthPresent = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .ge(AttendanceRecord::getAttendanceDate, monthStart)
                        .le(AttendanceRecord::getAttendanceDate, today)
                        .in(AttendanceRecord::getStatus, AttendanceStatus.PRESENT, AttendanceStatus.LATE));
        double thisMonthRate = thisMonthTotal == 0 ? 0.0 :
                Math.round((double) thisMonthPresent / thisMonthTotal * 1000.0) / 1000.0;

        // Last 7 days attendance
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardStatsResponse.DailyAttendance> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long dayTotal = attendanceRecordMapper.selectCount(
                    Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getAttendanceDate, date));
            long dayPresent = attendanceRecordMapper.selectCount(
                    Wrappers.<AttendanceRecord>lambdaQuery()
                            .eq(AttendanceRecord::getAttendanceDate, date)
                            .in(AttendanceRecord::getStatus, AttendanceStatus.PRESENT, AttendanceStatus.LATE));
            last7Days.add(new DashboardStatsResponse.DailyAttendance(date.format(fmt), dayPresent, dayTotal));
        }

        // Recent counts (last 30 days)
        LocalDate thirtyDaysAgo = today.minusDays(30);
        long recentFitness = fitnessTestRecordMapper.selectCount(
                Wrappers.<FitnessTestRecord>lambdaQuery().ge(FitnessTestRecord::getTestDate, thirtyDaysAgo));
        long recentTraining = trainingRecordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery().ge(TrainingRecord::getTrainingDate, thirtyDaysAgo));

        return new DashboardStatsResponse(
                new DashboardStatsResponse.StudentStats(studentTotal, studentActive, studentInactive, 0),
                new DashboardStatsResponse.CourseStats(courseTotal, coursePlanned, courseOngoing, courseCompleted, courseCancelled),
                new DashboardStatsResponse.CoachStats(coachTotal, coachActive, coachInactive),
                new DashboardStatsResponse.AttendanceStats(thisMonthTotal, thisMonthPresent, thisMonthRate, last7Days),
                recentFitness,
                recentTraining
        );
    }

    @Transactional(readOnly = true)
    public List<CoachWorkloadResponse> getCoachWorkload() {
        List<Coach> coaches = coachMapper.selectList(
                Wrappers.<Coach>lambdaQuery().eq(Coach::getStatus, CoachStatus.ACTIVE));
        List<CoachWorkloadResponse> result = new ArrayList<>();
        for (Coach coach : coaches) {
            long courseCount = courseMapper.selectCount(
                    Wrappers.<Course>lambdaQuery().eq(Course::getCoachName, coach.getName()));
            // Count training records via courses assigned to this coach
            List<Long> courseIds = courseMapper.selectList(
                    Wrappers.<Course>lambdaQuery().eq(Course::getCoachName, coach.getName())
                            .select(Course::getId))
                    .stream().map(Course::getId).toList();
            long trainingCount = courseIds.isEmpty() ? 0 : trainingRecordMapper.selectCount(
                    Wrappers.<TrainingRecord>lambdaQuery().in(TrainingRecord::getCourseId, courseIds));
            result.add(new CoachWorkloadResponse(coach.getId(), coach.getName(), courseCount, trainingCount));
        }
        return result;
    }
}
