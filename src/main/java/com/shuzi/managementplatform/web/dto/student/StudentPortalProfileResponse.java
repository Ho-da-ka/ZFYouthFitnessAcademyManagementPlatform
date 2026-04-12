package com.shuzi.managementplatform.web.dto.student;

import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentPortalProfileResponse(
        Long id,
        String studentNo,
        String name,
        Gender gender,
        LocalDate birthDate,
        String guardianName,
        String guardianPhone,
        StudentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
