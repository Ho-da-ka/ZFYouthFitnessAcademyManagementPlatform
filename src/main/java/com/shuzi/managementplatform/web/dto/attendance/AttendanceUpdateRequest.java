package com.shuzi.managementplatform.web.dto.attendance;

import com.shuzi.managementplatform.domain.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceUpdateRequest(
        @NotNull(message = "status is required") AttendanceStatus status,
        String note
) {}
