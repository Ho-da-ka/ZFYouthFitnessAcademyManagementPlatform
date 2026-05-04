package com.shuzi.managementplatform.web.dto.checkin;

import java.time.LocalDateTime;

public record CoachCheckinInitResponse(
        String token,
        Long courseId,
        String courseName,
        LocalDateTime expiresAt
) {
}
