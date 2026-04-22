package com.shuzi.managementplatform.web.dto.parentadmin;

import jakarta.validation.constraints.NotNull;

public record ParentManualBindingCreateRequest(
        @NotNull(message = "parentId is required")
        Long parentId,
        @NotNull(message = "studentId is required")
        Long studentId
) {
}
