package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.service.StageEvaluationService;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationCreateRequest;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationResponse;
import com.shuzi.managementplatform.web.dto.evaluation.StageEvaluationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stage-evaluations")
@Tag(name = "StageEvaluation", description = "Stage evaluation endpoints")
public class StageEvaluationController {

    private final StageEvaluationService stageEvaluationService;

    public StageEvaluationController(StageEvaluationService stageEvaluationService) {
        this.stageEvaluationService = stageEvaluationService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "Create stage evaluation")
    public ApiResponse<StageEvaluationResponse> create(@Valid @RequestBody StageEvaluationCreateRequest request) {
        return ApiResponse.ok("stage evaluation created", stageEvaluationService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PutMapping("/{id}")
    @Operation(summary = "Update stage evaluation")
    public ApiResponse<StageEvaluationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StageEvaluationUpdateRequest request
    ) {
        return ApiResponse.ok("stage evaluation updated", stageEvaluationService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get stage evaluation detail")
    public ApiResponse<StageEvaluationResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(stageEvaluationService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List stage evaluations with pagination")
    public ApiResponse<PageResponse<StageEvaluationResponse>> page(
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(stageEvaluationService.page(studentId, page, size)));
    }
}
