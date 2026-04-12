package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.config.CourseConflictChecker;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.TrainingRecord;
import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.domain.mapper.CourseMapper;
import com.shuzi.managementplatform.domain.mapper.TrainingRecordMapper;
import com.shuzi.managementplatform.web.dto.course.CourseAttendanceStatsResponse;
import com.shuzi.managementplatform.web.dto.course.CourseCreateRequest;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import com.shuzi.managementplatform.web.dto.course.CourseStudentResponse;
import com.shuzi.managementplatform.web.dto.course.CourseUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Course domain service for CRUD and filtered pagination.
 */
@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final StudentService studentService;
    private final CourseConflictChecker conflictChecker;

    public CourseService(CourseMapper courseMapper,
                         AttendanceRecordMapper attendanceRecordMapper,
                         TrainingRecordMapper trainingRecordMapper,
                         StudentService studentService,
                         CourseConflictChecker conflictChecker) {
        this.courseMapper = courseMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.trainingRecordMapper = trainingRecordMapper;
        this.studentService = studentService;
        this.conflictChecker = conflictChecker;
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request, boolean ignoreConflict) {
        // Business key uniqueness check.
        Long count = courseMapper.selectCount(
                Wrappers.<Course>lambdaQuery().eq(Course::getCourseCode, request.courseCode())
        );
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "courseCode already exists");
        }
        validateCourseTimeRange(request.courseDate(), request.classStartTime(), request.classEndTime());
        validateConflict(request.coachName(), request.courseDate(), request.classStartTime(),
                request.classEndTime(), null, ignoreConflict);
        Course course = new Course();
        course.setCourseCode(request.courseCode());
        course.setName(request.name());
        course.setCourseType(request.courseType());
        course.setCoachName(request.coachName());
        course.setVenue(request.venue());
        course.setStartTime(request.startTime());
        course.setDurationMinutes(request.durationMinutes());
        course.setMaxCapacity(request.maxCapacity());
        course.setCourseDate(request.courseDate());
        course.setClassStartTime(request.classStartTime());
        course.setClassEndTime(request.classEndTime());
        course.setStatus(request.status() == null ? CourseStatus.PLANNED : request.status());
        course.setDescription(request.description());
        course.setTrainingTheme(request.trainingTheme());
        course.setTargetAgeRange(request.targetAgeRange());
        course.setTargetGoals(request.targetGoals());
        course.setFocusPoints(request.focusPoints());
        courseMapper.insert(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request, boolean ignoreConflict) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        validateCourseTimeRange(request.courseDate(), request.classStartTime(), request.classEndTime());
        validateConflict(request.coachName(), request.courseDate(), request.classStartTime(),
                request.classEndTime(), id, ignoreConflict);
        course.setName(request.name());
        course.setCourseType(request.courseType());
        course.setCoachName(request.coachName());
        course.setVenue(request.venue());
        course.setStartTime(request.startTime());
        course.setDurationMinutes(request.durationMinutes());
        course.setMaxCapacity(request.maxCapacity());
        course.setCourseDate(request.courseDate());
        course.setClassStartTime(request.classStartTime());
        course.setClassEndTime(request.classEndTime());
        course.setStatus(request.status());
        course.setDescription(request.description());
        course.setTrainingTheme(request.trainingTheme());
        course.setTargetAgeRange(request.targetAgeRange());
        course.setTargetGoals(request.targetGoals());
        course.setFocusPoints(request.focusPoints());
        courseMapper.updateById(course);
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public IPage<CourseResponse> page(String name, CourseStatus status, int page, int size) {
        // MyBatis-Plus page index starts from 1; API contract uses 0-based index.
        Page<Course> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<Course> query = Wrappers.<Course>lambdaQuery();

        if (StringUtils.hasText(name)) {
            query.like(Course::getName, name.trim());
        }
        if (status != null) {
            query.eq(Course::getStatus, status);
        }
        query.orderByDesc(Course::getId);

        Page<Course> result = courseMapper.selectPage(pageRequest, query);
        List<CourseResponse> records = result.getRecords().stream().map(this::toResponse).toList();

        Page<CourseResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(records);
        return responsePage;
    }

    @Transactional
    public void delete(Long id, boolean force) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        long attendanceCount = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getCourseId, id));
        long trainingCount = trainingRecordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery().eq(TrainingRecord::getCourseId, id));
        if (!force && (attendanceCount > 0 || trainingCount > 0)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "课程存在关联数据（考勤:" + attendanceCount + "，训练:" + trainingCount + "），请先删除关联数据或使用强制删除");
        }
        if (force) {
            attendanceRecordMapper.delete(
                    Wrappers.<AttendanceRecord>lambdaQuery().eq(AttendanceRecord::getCourseId, id));
            trainingRecordMapper.delete(
                    Wrappers.<TrainingRecord>lambdaQuery().eq(TrainingRecord::getCourseId, id));
        }
        courseMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Course getEntityById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ResourceNotFoundException("course not found: " + id);
        }
        return course;
    }

    @Transactional(readOnly = true)
    public List<CourseStudentResponse> getCourseStudents(Long id) {
        getEntityById(id);
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getCourseId, id)
                        .orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
        );
        Map<Long, List<AttendanceRecord>> grouped = new LinkedHashMap<>();
        for (AttendanceRecord record : records) {
            grouped.computeIfAbsent(record.getStudentId(), key -> new ArrayList<>()).add(record);
        }

        List<CourseStudentResponse> result = new ArrayList<>();
        grouped.forEach((studentId, attendanceList) -> {
            Student student = studentService.findNullableById(studentId);
            if (student == null) {
                return;
            }
            long total = attendanceList.size();
            long present = attendanceList.stream()
                    .filter(item -> item.getStatus() == AttendanceStatus.PRESENT || item.getStatus() == AttendanceStatus.LATE)
                    .count();
            double rate = total == 0 ? 0.0 : Math.round((double) present / total * 1000.0) / 1000.0;
            result.add(new CourseStudentResponse(
                    student.getId(),
                    student.getName(),
                    student.getGender() == null ? "" : student.getGender().name(),
                    total,
                    present,
                    rate
            ));
        });
        result.sort(Comparator.comparing(CourseStudentResponse::studentName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    @Transactional(readOnly = true)
    public CourseAttendanceStatsResponse getCourseAttendanceStats(Long id) {
        getEntityById(id);
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getCourseId, id)
                        .orderByAsc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId)
        );
        Map<LocalDate, long[]> daily = new LinkedHashMap<>();
        long presentTotal = 0;
        for (AttendanceRecord record : records) {
            daily.computeIfAbsent(record.getAttendanceDate(), key -> new long[]{0, 0});
            long[] counts = daily.get(record.getAttendanceDate());
            counts[1] += 1;
            if (record.getStatus() == AttendanceStatus.PRESENT || record.getStatus() == AttendanceStatus.LATE) {
                counts[0] += 1;
                presentTotal += 1;
            }
        }
        List<CourseAttendanceStatsResponse.DailyAttendance> trend = new ArrayList<>();
        daily.forEach((date, counts) -> trend.add(
                new CourseAttendanceStatsResponse.DailyAttendance(date, counts[0], counts[1])
        ));
        long total = records.size();
        double avgRate = total == 0 ? 0.0 : Math.round((double) presentTotal / total * 1000.0) / 1000.0;
        return new CourseAttendanceStatsResponse(daily.size(), avgRate, trend);
    }

    private CourseResponse toResponse(Course course) {
        long currentEnrollment = countDistinctStudents(course.getId());
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getName(),
                course.getCourseType(),
                course.getCoachName(),
                course.getVenue(),
                course.getStartTime(),
                course.getDurationMinutes(),
                course.getMaxCapacity(),
                currentEnrollment,
                course.getCourseDate(),
                course.getClassStartTime(),
                course.getClassEndTime(),
                course.getStatus(),
                course.getDescription(),
                course.getTrainingTheme(),
                course.getTargetAgeRange(),
                course.getTargetGoals(),
                course.getFocusPoints(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public long countDistinctStudents(Long courseId) {
        if (courseId == null) {
            return 0;
        }
        return attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getCourseId, courseId)
                        .select(AttendanceRecord::getStudentId)
        ).stream()
                .map(AttendanceRecord::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    private void validateCourseTimeRange(LocalDate courseDate, LocalTime classStartTime, LocalTime classEndTime) {
        if ((classStartTime == null) != (classEndTime == null)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "开始时间和结束时间需同时填写");
        }
        if (classStartTime != null && classEndTime != null && !classEndTime.isAfter(classStartTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        if (courseDate == null && classStartTime != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "填写上课时间段时必须选择上课日期");
        }
    }

    private void validateConflict(String coachName,
                                  LocalDate courseDate,
                                  LocalTime classStartTime,
                                  LocalTime classEndTime,
                                  Long excludeId,
                                  boolean ignoreConflict) {
        List<Course> conflicts = conflictChecker.findConflicts(
                coachName, courseDate, classStartTime, classEndTime, excludeId);
        if (!ignoreConflict && !conflicts.isEmpty()) {
            String conflictNames = conflicts.stream()
                    .limit(3)
                    .map(Course::getName)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining("、"));
            if (!StringUtils.hasText(conflictNames)) {
                conflictNames = "已有课程";
            }
            throw new BusinessException(HttpStatus.CONFLICT, "教练时间冲突：" + conflictNames);
        }
    }
}
