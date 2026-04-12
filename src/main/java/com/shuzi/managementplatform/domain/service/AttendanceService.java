package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzi.managementplatform.common.exception.ResourceNotFoundException;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Attendance service handles write/query operations for class attendance records.
 */
@Service
public class AttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StudentService studentService;
    private final CourseService courseService;

    public AttendanceService(
            AttendanceRecordMapper attendanceRecordMapper,
            StudentService studentService,
            CourseService courseService
    ) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional
    public AttendanceResponse create(AttendanceCreateRequest request) {
        Student student = studentService.getEntityById(request.studentId());
        Course course = courseService.getEntityById(request.courseId());

        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(student.getId());
        record.setCourseId(course.getId());
        record.setAttendanceDate(request.attendanceDate());
        record.setStatus(request.status());
        record.setNote(request.note());
        attendanceRecordMapper.insert(record);
        return toResponse(record, student, course);
    }

    @Transactional
    public void delete(Long id) {
        AttendanceRecord record = attendanceRecordMapper.selectById(id);
        if (record == null) {
            throw new ResourceNotFoundException("attendance record not found: " + id);
        }
        attendanceRecordMapper.deleteById(id);
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
}
