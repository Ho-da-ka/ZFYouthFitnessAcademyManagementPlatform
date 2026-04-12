package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.common.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import org.springframework.http.HttpStatus;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceBatchCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Attendance service handles write/query operations for class attendance records.
 */
@Service
public class AttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StudentService studentService;
    private final CourseService courseService;
    private final CareAlertService careAlertService;

    public AttendanceService(
            AttendanceRecordMapper attendanceRecordMapper,
            StudentService studentService,
            CourseService courseService,
            CareAlertService careAlertService
    ) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentService = studentService;
        this.courseService = courseService;
        this.careAlertService = careAlertService;
    }

    @Transactional
    public AttendanceResponse create(AttendanceCreateRequest request) {
        validateAttendanceDate(request.attendanceDate());
        ensureNoDuplicateAttendance(request.courseId(), request.studentId(), request.attendanceDate());
        ensureCourseCapacityAvailable(request.courseId(), request.studentId());

        Student student = studentService.getEntityById(request.studentId());
        Course course = courseService.getEntityById(request.courseId());

        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(student.getId());
        record.setCourseId(course.getId());
        record.setAttendanceDate(request.attendanceDate());
        record.setStatus(request.status());
        record.setNote(request.note());
        attendanceRecordMapper.insert(record);
        careAlertService.refreshStudentAlerts(request.studentId());
        return toResponse(record, student, course);
    }

    @Transactional
    public List<AttendanceResponse> batchCreate(AttendanceBatchCreateRequest request) {
        validateAttendanceDate(request.attendanceDate());
        Course course = courseService.getEntityById(request.courseId());
        List<AttendanceResponse> result = new ArrayList<>();
        LinkedHashSet<Long> deduplicatedStudentIds = new LinkedHashSet<>(request.studentIds());
        for (Long studentId : deduplicatedStudentIds) {
            Student student = studentService.getEntityById(studentId);
            Long exists = attendanceRecordMapper.selectCount(
                    Wrappers.<AttendanceRecord>lambdaQuery()
                            .eq(AttendanceRecord::getCourseId, request.courseId())
                            .eq(AttendanceRecord::getStudentId, studentId)
                            .eq(AttendanceRecord::getAttendanceDate, request.attendanceDate())
            );
            if (exists != null && exists > 0) {
                continue;
            }
            ensureCourseCapacityAvailable(request.courseId(), studentId);
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(studentId);
            record.setCourseId(request.courseId());
            record.setAttendanceDate(request.attendanceDate());
            record.setStatus(request.status());
            attendanceRecordMapper.insert(record);
            result.add(toResponse(record, student, course));
        }
        for (Long studentId : deduplicatedStudentIds) {
            careAlertService.refreshStudentAlerts(studentId);
        }
        return result;
    }

    @Transactional
    public void delete(Long id) {
        AttendanceRecord record = attendanceRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("attendance record not found: " + id);
        }
        attendanceRecordMapper.deleteById(id);
        careAlertService.refreshStudentAlerts(record.getStudentId());
    }

    @Transactional
    public AttendanceResponse update(Long id, AttendanceUpdateRequest request) {
        AttendanceRecord record = attendanceRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("attendance record not found: " + id);
        }
        record.setStatus(request.status());
        record.setNote(request.note());
        attendanceRecordMapper.updateById(record);
        careAlertService.refreshStudentAlerts(record.getStudentId());
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public IPage<AttendanceResponse> page(Long studentId, Long courseId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Page<AttendanceRecord> pageRequest = new Page<>(page + 1L, size);
        LambdaQueryWrapper<AttendanceRecord> query = Wrappers.<AttendanceRecord>lambdaQuery();
        if (studentId != null) query.eq(AttendanceRecord::getStudentId, studentId);
        if (courseId != null) query.eq(AttendanceRecord::getCourseId, courseId);
        if (startDate != null) query.ge(AttendanceRecord::getAttendanceDate, startDate);
        if (endDate != null) query.le(AttendanceRecord::getAttendanceDate, endDate);
        query.orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId);

        Page<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageRequest, query);
        Page<AttendanceResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        Student student = studentService.getEntityById(record.getStudentId());
        Course course = courseService.getEntityById(record.getCourseId());
        return toResponse(record, student, course);
    }

    private AttendanceResponse toResponse(AttendanceRecord record, Student student, Course course) {
        return new AttendanceResponse(
                record.getId(),
                student.getId(),
                student.getName(),
                course.getId(),
                course.getName(),
                record.getAttendanceDate(),
                record.getStatus(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private void validateAttendanceDate(LocalDate attendanceDate) {
        if (attendanceDate != null && attendanceDate.isAfter(LocalDate.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "考勤日期不能晚于当前日期");
        }
    }

    private void ensureNoDuplicateAttendance(Long courseId, Long studentId, LocalDate attendanceDate) {
        Long count = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getCourseId, courseId)
                        .eq(AttendanceRecord::getStudentId, studentId)
                        .eq(AttendanceRecord::getAttendanceDate, attendanceDate)
        );
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "同一学员同一课程同一天已存在考勤记录");
        }
    }

    private void ensureCourseCapacityAvailable(Long courseId, Long studentId) {
        Course course = courseService.getEntityById(courseId);
        Integer maxCapacity = course.getMaxCapacity();
        if (maxCapacity == null || maxCapacity <= 0) {
            return;
        }
        Long studentCount = attendanceRecordMapper.selectCount(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getCourseId, courseId)
                        .eq(AttendanceRecord::getStudentId, studentId)
        );
        if (studentCount != null && studentCount > 0) {
            return;
        }
        long enrolled = courseService.countDistinctStudents(courseId);
        if (enrolled >= maxCapacity) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "课程报名已达上限（" + enrolled + "/" + maxCapacity + "）");
        }
    }
}
