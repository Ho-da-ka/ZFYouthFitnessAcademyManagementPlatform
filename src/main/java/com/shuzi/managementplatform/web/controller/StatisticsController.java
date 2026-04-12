package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.StatisticsService;
import com.shuzi.managementplatform.web.dto.statistics.CoachWorkloadResponse;
import com.shuzi.managementplatform.web.dto.statistics.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@Tag(name = "Statistics", description = "Dashboard statistics endpoints")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ApiResponse<DashboardStatsResponse> dashboard() {
        return ApiResponse.ok(statisticsService.getDashboard());
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/coach-workload")
    @Operation(summary = "Get coach workload statistics")
    public ApiResponse<List<CoachWorkloadResponse>> coachWorkload() {
        return ApiResponse.ok(statisticsService.getCoachWorkload());
    }
}
