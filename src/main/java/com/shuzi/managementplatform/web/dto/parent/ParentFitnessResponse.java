package com.shuzi.managementplatform.web.dto.parent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ParentFitnessResponse(
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

