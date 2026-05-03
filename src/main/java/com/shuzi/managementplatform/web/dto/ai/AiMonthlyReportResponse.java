package com.shuzi.managementplatform.web.dto.ai;

import java.time.LocalDateTime;

public record AiMonthlyReportResponse(
        LocalDateTime generatedAt,
        String report,
        boolean generatedByAi
) {}
