package com.shuzi.managementplatform.web.dto.parent;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParentBookingCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,
        @NotNull(message = "courseId is required")
        Long courseId,
        @Size(max = 255, message = "remark length must be <= 255")
        String remark
) {
}

