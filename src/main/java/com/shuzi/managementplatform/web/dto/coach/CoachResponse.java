package com.shuzi.managementplatform.web.dto.coach;

import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.Gender;

import java.time.LocalDateTime;

public record CoachResponse(
        Long id,
        String coachCode,
        String name,
        Gender gender,
        String phone,
        String specialty,
        CoachStatus status,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
