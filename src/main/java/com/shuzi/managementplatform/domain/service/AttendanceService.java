package com.shuzi.managementplatform.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.entity.Course;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.mapper.AttendanceRecordMapper;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<AttendanceResponse> search(Long studentId, Long courseId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AttendanceRecord> query = Wrappers.<AttendanceRecord>lambdaQuery();
        if (studentId != null) {
            query.eq(AttendanceRecord::getStudentId, studentId);
        }
        if (courseId != null) {
            query.eq(AttendanceRecord::getCourseId, courseId);
        }
        if (startDate != null) {
            query.ge(AttendanceRecord::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            query.le(AttendanceRecord::getAttendanceDate, endDate);
        }
        query.orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getId);

        return attendanceRecordMapper.selectList(query)
                .stream()
                .map(this::toResponse)
                .toList();
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
