package com.shuzi.managementplatform.web.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record AiTrainingSummaryRequest(
        @NotBlank String trainingContent,
        String highlightNote,
        String improvementNote,
        String parentAction,
        String nextStepSuggestion
) {}
