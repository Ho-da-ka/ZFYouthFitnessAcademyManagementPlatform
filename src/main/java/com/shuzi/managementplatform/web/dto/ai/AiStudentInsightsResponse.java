package com.shuzi.managementplatform.web.dto.ai;

import java.util.List;

public record AiStudentInsightsResponse(
        String summary,
        List<StrengthPoint> strengths,
        List<String> suggestions
) {
    public record StrengthPoint(String item, String level, String description) {}
}
