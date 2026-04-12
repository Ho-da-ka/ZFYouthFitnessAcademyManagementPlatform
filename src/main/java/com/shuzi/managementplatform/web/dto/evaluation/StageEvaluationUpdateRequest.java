package com.shuzi.managementplatform.web.dto.evaluation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StageEvaluationUpdateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        @NotBlank(message = "cycleName is required")
        @Size(max = 100, message = "cycleName max length is 100")
        String cycleName,

        @NotNull(message = "periodStart is required")
        LocalDate periodStart,

        @NotNull(message = "periodEnd is required")
        LocalDate periodEnd,

        @NotBlank(message = "trainingSummary is required")
        @Size(max = 255, message = "trainingSummary max length is 255")
        String trainingSummary,

        @NotBlank(message = "fitnessSummary is required")
        @Size(max = 255, message = "fitnessSummary max length is 255")
        String fitnessSummary,

        @NotBlank(message = "coachEvaluation is required")
        @Size(max = 500, message = "coachEvaluation max length is 500")
        String coachEvaluation,

        @NotBlank(message = "nextStagePlan is required")
        @Size(max = 500, message = "nextStagePlan max length is 500")
        String nextStagePlan
) {
}
