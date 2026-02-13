package com.shuzi.managementplatform.web.dto.fitness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FitnessTestResponse(
        Long id,
        Long studentId,
        String studentName,
        LocalDate testDate,
        String itemName,
        BigDecimal testValue,
        String unit,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
