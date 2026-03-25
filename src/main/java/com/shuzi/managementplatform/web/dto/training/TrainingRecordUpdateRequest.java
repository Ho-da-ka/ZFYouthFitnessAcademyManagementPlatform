package com.shuzi.managementplatform.web.dto.training;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TrainingRecordUpdateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        @NotNull(message = "courseId is required")
        Long courseId,

        @NotNull(message = "trainingDate is required")
        LocalDate trainingDate,

        @NotBlank(message = "trainingContent is required")
        @Size(max = 255, message = "trainingContent max length is 255")
        String trainingContent,

        @NotNull(message = "durationMinutes is required")
        @Min(value = 1, message = "durationMinutes must be greater than or equal to 1")
        Integer durationMinutes,

        @Size(max = 32, message = "intensityLevel max length is 32")
        String intensityLevel,

        @Size(max = 255, message = "performanceSummary max length is 255")
        String performanceSummary,

        @Size(max = 255, message = "coachComment max length is 255")
        String coachComment
) {
}
