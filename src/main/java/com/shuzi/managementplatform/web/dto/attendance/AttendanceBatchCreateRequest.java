package com.shuzi.managementplatform.web.dto.attendance;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AttendanceBatchCreateRequest(
        @NotNull Long courseId,
        @NotNull LocalDate attendanceDate,
        @NotNull @Size(min = 1) List<Long> studentIds,
        @NotNull AttendanceStatus status
) {
}

