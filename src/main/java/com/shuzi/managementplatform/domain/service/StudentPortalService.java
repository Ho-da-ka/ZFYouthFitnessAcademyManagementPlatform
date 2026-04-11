package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.CourseBooking;
import com.shuzi.managementplatform.domain.entity.FitnessTestRecord;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseBookingMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.FitnessTestRecordMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
import com.shuzi.managementplatform.web.dto.student.StudentCourseResponse;
import com.shuzi.managementplatform.web.dto.student.StudentProfileResponse;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Student-side mini-program portal service for profile, courses, training and fitness records.
 */
@Service
public class StudentPortalService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String BOOKING_STATUS_BOOKED = "BOOKED";

    private final UserAccountMapper userAccountMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final FitnessTestRecordMapper fitnessTestRecordMapper;
    private final CourseBookingMapper courseBookingMapper;

    public StudentPortalService(
            UserAccountMapper userAccountMapper,
            StudentMapper studentMapper,
            CourseMapper courseMapper,
            AttendanceRecordMapper attendanceRecordMapper,
            TrainingRecordMapper trainingRecordMapper,
            FitnessTestRecordMapper fitnessTestRecordMapper,
            CourseBookingMapper courseBookingMapper
    ) {
        this.userAccountMapper = userAccountMapper;
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.fitnessTestRecordMapper = fitnessTestRecordMapper;
        this.courseBookingMapper = courseBookingMapper;
    }

    @Transactional
    public StudentProfileResponse getProfile(String username) {
        Student student = resolveStudent(username);
        return new StudentProfileResponse(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getBirthDate(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getStatus(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    @Transactional
    public List<StudentCourseResponse> listCourses(String username) {
        Student student = resolveStudent(username);

        List<AttendanceRecord> attendances = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getStudentId, student.getId())
                        .orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
        );
        List<TrainingRecord> trainings = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getStudentId, student.getId())
                        .orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId)
        );
        List<CourseBooking> bookings = courseBookingMapper.selectList(
                Wrappers.<CourseBooking>lambdaQuery()
                        .eq(CourseBooking::getStudentId, student.getId())
                        .orderByDesc(CourseBooking::getId)
        );

        LinkedHashSet<Long> courseIds = new LinkedHashSet<>();
        attendances.forEach(item -> courseIds.add(item.getCourseId()));
        trainings.forEach(item -> courseIds.add(item.getCourseId()));
        bookings.forEach(item -> courseIds.add(item.getCourseId()));
        if (courseIds.isEmpty()) {
            return List.of();
        }

        List<Course> courses = new ArrayList<>(courseMapper.selectBatchIds(courseIds));
        Map<Long, CourseBooking> latestBookingByCourse = new HashMap<>();
        for (CourseBooking booking : bookings) {
            latestBookingByCourse.putIfAbsent(booking.getCourseId(), booking);
        }

        courses.sort(Comparator
                .comparing(Course::getStartTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Course::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        return courses.stream().map(course -> {
            CourseBooking booking = latestBookingByCourse.get(course.getId());
            return new StudentCourseResponse(
                    course.getId(),
                    course.getCourseCode(),
                    course.getName(),
                    course.getCourseType(),
                    course.getCoachName(),
                    course.getVenue(),
                    course.getStartTime(),
                    course.getDurationMinutes(),
                    course.getStatus(),
                    course.getDescription(),
                    booking == null ? null : booking.getBookingStatus(),
                    booking == null ? null : booking.getCheckinStatus()
            );
        }).toList();
    }

    @Transactional
    public List<TrainingRecordResponse> listTrainingRecords(String username) {
        Student student = resolveStudent(username);
        List<TrainingRecord> records = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getStudentId, student.getId())
                        .orderByDesc(TrainingRecord::getTrainingDate, TrainingRecord::getId)
        );
        if (records.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Long> courseIds = new LinkedHashSet<>();
        records.forEach(item -> courseIds.add(item.getCourseId()));
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course course : courseMapper.selectBatchIds(courseIds)) {
            courseMap.put(course.getId(), course);
        }

        return records.stream().map(record -> {
            Course course = courseMap.get(record.getCourseId());
            return new TrainingRecordResponse(
                    record.getId(),
                    record.getStudentId(),
                    student.getName(),
                    record.getCourseId(),
                    course == null ? "-" : course.getName(),
                    record.getTrainingDate(),
                    record.getTrainingContent(),
                    record.getDurationMinutes(),
                    record.getIntensityLevel(),
                    record.getPerformanceSummary(),
                    record.getCoachComment(),
                    record.getCreatedAt(),
                    record.getUpdatedAt()
            );
        }).toList();
    }

    @Transactional
    public List<FitnessTestResponse> listFitnessTests(String username) {
        Student student = resolveStudent(username);
        List<FitnessTestRecord> records = fitnessTestRecordMapper.selectList(
                Wrappers.<FitnessTestRecord>lambdaQuery()
                        .eq(FitnessTestRecord::getStudentId, student.getId())
                        .orderByDesc(FitnessTestRecord::getTestDate, FitnessTestRecord::getId)
        );
        return records.stream().map(record -> new FitnessTestResponse(
                record.getId(),
                record.getStudentId(),
                student.getName(),
                record.getTestDate(),
                record.getItemName(),
                record.getTestValue(),
                record.getUnit(),
                record.getComment(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        )).toList();
    }

    private Student resolveStudent(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录状态已失效");
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, normalizedUsername)
        );
        if (account == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录账号不存在");
        }
        if (!ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号不是学生角色");
        }
        if (!STATUS_ACTIVE.equalsIgnoreCase(account.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "当前账号已禁用");
        }

        Long studentId = account.getStudentId();
        if (studentId == null || studentId <= 0) {
            studentId = bindDefaultStudent(account);
        }
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("student not found: " + studentId);
        }
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "学员状态不可用");
        }
        return student;
    }

    private Long bindDefaultStudent(UserAccount account) {
        if (!"student".equals(account.getUsername())) {
            throw new BusinessException(HttpStatus.CONFLICT, "当前学生账号未绑定学员档案");
        }
        Student firstActiveStudent = studentMapper.selectOne(
                Wrappers.<Student>lambdaQuery()
                        .eq(Student::getStatus, StudentStatus.ACTIVE)
                        .orderByAsc(Student::getId)
                        .last("limit 1")
        );
        if (firstActiveStudent == null) {
            throw new BusinessException(HttpStatus.CONFLICT, "暂无可绑定的在训学员");
        }
        return firstActiveStudent.getId();
    }
}
