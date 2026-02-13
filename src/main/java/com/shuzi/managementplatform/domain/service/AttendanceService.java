package com.shuzi.managementplatform.domain.service;

import com.shuzi.managementplatform.domain.entity.AttendanceRecord;
import com.shuzi.managementplatform.domain.repository.AttendanceRecordRepository;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    public AttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            StudentService studentService,
            CourseService courseService
    ) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional
    public AttendanceResponse create(AttendanceCreateRequest request) {
        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(studentService.getEntityById(request.studentId()));
        record.setCourse(courseService.getEntityById(request.courseId()));
        record.setAttendanceDate(request.attendanceDate());
        record.setStatus(request.status());
        record.setNote(request.note());
        return toResponse(attendanceRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> search(Long studentId, Long courseId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.search(studentId, courseId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        return new AttendanceResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getName(),
                record.getCourse().getId(),
                record.getCourse().getName(),
                record.getAttendanceDate(),
                record.getStatus(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
