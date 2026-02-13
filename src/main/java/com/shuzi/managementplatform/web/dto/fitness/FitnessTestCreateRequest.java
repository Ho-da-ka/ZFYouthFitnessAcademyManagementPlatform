package com.shuzi.managementplatform.web.dto.fitness;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FitnessTestCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        @NotNull(message = "testDate is required")
        LocalDate testDate,

        @NotBlank(message = "itemName is required")
        @Size(max = 100, message = "itemName max length is 100")
        String itemName,

        @NotNull(message = "testValue is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "testValue must be greater than 0")
        BigDecimal testValue,

        @NotBlank(message = "unit is required")
        @Size(max = 32, message = "unit max length is 32")
        String unit,

        @Size(max = 255, message = "comment max length is 255")
        String comment
) {
}
