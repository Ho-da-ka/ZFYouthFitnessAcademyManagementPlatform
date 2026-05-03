package com.shuzi.managementplatform.web.dto.parent;

import java.time.LocalDateTime;

public record ParentAiGrowthReportResponse(
        String report,
        boolean generatedByAi,
        LocalDateTime generatedAt
) {
}
