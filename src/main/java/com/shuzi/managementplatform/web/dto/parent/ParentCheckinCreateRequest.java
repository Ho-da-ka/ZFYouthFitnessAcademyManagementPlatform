package com.shuzi.managementplatform.web.dto.parent;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ParentCheckinCreateRequest(
        @NotNull(message = "bookingId is required")
        Long bookingId,
        LocalDate attendanceDate,
        String note
) {
}

