package com.shuzi.managementplatform.web.dto.parent;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ParentCheckinResponse(
        Long id,
        Long bookingId,
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

