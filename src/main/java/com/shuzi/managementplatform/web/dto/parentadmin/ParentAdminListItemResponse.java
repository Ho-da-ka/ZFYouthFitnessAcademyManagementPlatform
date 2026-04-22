package com.shuzi.managementplatform.web.dto.parentadmin;

import java.time.LocalDateTime;
import java.util.List;

public record ParentAdminListItemResponse(
        Long id,
        String displayName,
        String phone,
        String username,
        LocalDateTime lastLoginAt,
        LocalDateTime updatedAt,
        int studentCount,
        List<String> studentNames
) {
}
