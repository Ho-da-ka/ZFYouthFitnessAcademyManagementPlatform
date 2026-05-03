package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.AiAnalysisService;
import com.shuzi.managementplatform.domain.service.GeneratedContentService;
import com.shuzi.managementplatform.web.dto.ai.AiHubOverviewResponse;
import com.shuzi.managementplatform.web.dto.ai.AiMonthlyReportResponse;
import com.shuzi.managementplatform.web.dto.ai.AiStudentInsightsResponse;
import com.shuzi.managementplatform.web.dto.ai.AiTrainingSummaryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AiAnalysis", description = "AI analysis and generation endpoints")
public class AiAnalysisController {

    private final GeneratedContentService generatedContentService;
    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(GeneratedContentService generatedContentService, AiAnalysisService aiAnalysisService) {
        this.generatedContentService = generatedContentService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping("/generate-training-summary")
    @Operation(summary = "Generate training summary using AI")
    public ApiResponse<String> generateTrainingSummary(@Valid @RequestBody AiTrainingSummaryRequest request) {
        String result = generatedContentService.generateTrainingSummary(
                request.trainingContent(),
                request.highlightNote(),
                request.improvementNote(),
                request.parentAction(),
                request.nextStepSuggestion()
        );
        return ApiResponse.ok(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH','PARENT')")
    @GetMapping("/student-insights/{studentId}")
    @Operation(summary = "Get AI-driven student insights")
    public ApiResponse<AiStudentInsightsResponse> getStudentInsights(@PathVariable Long studentId) {
        return ApiResponse.ok(aiAnalysisService.getStudentInsights(studentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/hub-overview")
    @Operation(summary = "Get AI hub overview generated from real operational data")
    public ApiResponse<AiHubOverviewResponse> getHubOverview() {
        return ApiResponse.ok(aiAnalysisService.getHubOverview());
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping("/monthly-report")
    @Operation(summary = "Generate monthly school report using AI")
    public ApiResponse<AiMonthlyReportResponse> generateMonthlyReport() {
        return ApiResponse.ok(aiAnalysisService.generateMonthlyReport());
    }
}
