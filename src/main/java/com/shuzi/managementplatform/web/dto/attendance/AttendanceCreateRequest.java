package com.shuzi.managementplatform.web.dto.attendance;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AttendanceCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        @NotNull(message = "courseId is required")
        Long courseId,

        @NotNull(message = "attendanceDate is required")
        LocalDate attendanceDate,

        @NotNull(message = "status is required")
        AttendanceStatus status,

        @Size(max = 255, message = "note max length is 255")
        String note
) {
}
