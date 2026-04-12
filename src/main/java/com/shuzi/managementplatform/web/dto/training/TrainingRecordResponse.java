package com.shuzi.managementplatform.web.dto.training;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TrainingRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseName,
        LocalDate trainingDate,
        String trainingContent,
        Integer durationMinutes,
        String intensityLevel,
        String performanceSummary,
        String highlightNote,
        String improvementNote,
        String parentAction,
        String nextStepSuggestion,
        String coachComment,
        String aiSummary,
        LocalDateTime parentReadAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
