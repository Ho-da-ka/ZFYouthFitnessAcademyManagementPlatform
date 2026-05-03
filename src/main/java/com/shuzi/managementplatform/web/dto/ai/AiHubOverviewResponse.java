package com.shuzi.managementplatform.web.dto.ai;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiHubOverviewResponse(
        LocalDateTime generatedAt,
        List<Metric> metrics,
        List<GrowthAlert> alerts,
        List<RadarDimension> radarDimensions
) {
    public record Metric(
            String key,
            String label,
            String value,
            String unit,
            String footer,
            String accent
    ) {}

    public record GrowthAlert(
            String id,
            String type,
            String title,
            String desc,
            String suggestedAction,
            String routeName,
            Map<String, String> routeQuery,
            boolean generatedByAi
    ) {}

    public record RadarDimension(String name, int value) {}
}
