package com.shuzi.managementplatform.web.dto.student;

import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String studentNo,
        String name,
        Gender gender,
        LocalDate birthDate,
        String guardianName,
        String guardianPhone,
        StudentStatus status,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
