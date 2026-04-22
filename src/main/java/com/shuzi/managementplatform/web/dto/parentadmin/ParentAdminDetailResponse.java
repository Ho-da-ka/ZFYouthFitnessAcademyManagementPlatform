package com.shuzi.managementplatform.web.dto.parentadmin;

import java.time.LocalDateTime;
import java.util.List;

public record ParentAdminDetailResponse(
        Long id,
        String displayName,
        String phone,
        String username,
        LocalDateTime lastLoginAt,
        LocalDateTime updatedAt,
        List<ParentAdminBoundStudentResponse> students
) {
}
