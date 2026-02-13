package com.shuzi.managementplatform.web.dto.attendance;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseName,
        LocalDate attendanceDate,
        AttendanceStatus status,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
