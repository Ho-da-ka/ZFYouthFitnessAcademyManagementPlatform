package com.shuzi.managementplatform.web.dto.parent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ParentGrowthOverviewResponse(
        Long studentId,
        String studentName,
        String goalFocus,
        String trainingTags,
        String riskNotes,
        LocalDate goalStartDate,
        LocalDate goalEndDate,
        List<TrainingFeedbackItem> recentTrainingFeedback,
        GrowthEvaluation latestEvaluation
) {
    public record TrainingFeedbackItem(
            Long id,
            LocalDate trainingDate,
            String trainingContent,
            String highlightNote,
            String improvementNote,
            String parentAction,
            String nextStepSuggestion,
            String aiSummary,
            LocalDateTime parentReadAt
    ) {
    }

    public record GrowthEvaluation(
            String cycleName,
            double attendanceRate,
            String fitnessSummary,
            String coachEvaluation,
            String nextStagePlan,
            String parentReport
    ) {
    }
}
