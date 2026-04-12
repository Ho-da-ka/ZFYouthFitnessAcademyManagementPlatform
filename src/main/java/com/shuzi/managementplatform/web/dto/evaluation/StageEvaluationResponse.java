package com.shuzi.managementplatform.web.dto.evaluation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StageEvaluationResponse(
        Long id,
        Long studentId,
        String studentName,
        String cycleName,
        LocalDate periodStart,
        LocalDate periodEnd,
        double attendanceRate,
        String trainingSummary,
        String fitnessSummary,
        String coachEvaluation,
        String nextStagePlan,
        String aiInterpretation,
        String parentReport,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
