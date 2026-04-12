package com.shuzi.managementplatform.web.dto.alert;

import java.time.LocalDateTime;

public record CareAlertResponse(
        Long id,
        Long studentId,
        String studentName,
        String alertType,
        String alertTitle,
        String alertContent,
        String status,
        LocalDateTime triggeredAt,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
