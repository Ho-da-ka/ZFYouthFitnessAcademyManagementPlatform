package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.service.TrainingRecordService;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordCreateRequest;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordResponse;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Training record endpoints.
 */
@RestController
@RequestMapping("/api/v1/training-records")
@Tag(name = "TrainingRecord", description = "Training record endpoints")
public class TrainingRecordController {

    private final TrainingRecordService trainingRecordService;

    public TrainingRecordController(TrainingRecordService trainingRecordService) {
        this.trainingRecordService = trainingRecordService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "Create training record", description = "Save one training session record for a student")
    public ApiResponse<TrainingRecordResponse> create(@Valid @RequestBody TrainingRecordCreateRequest request) {
        return ApiResponse.ok("training record created", trainingRecordService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PutMapping("/{id}")
    @Operation(summary = "Update training record", description = "Update one existing training session record")
    public ApiResponse<TrainingRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TrainingRecordUpdateRequest request
    ) {
        return ApiResponse.ok("training record updated", trainingRecordService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get training record detail", description = "Get one training session record by ID")
    public ApiResponse<TrainingRecordResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(trainingRecordService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete training record")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        trainingRecordService.delete(id);
        return ApiResponse.ok("training record deleted", null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List training records with pagination")
    public ApiResponse<PageResponse<TrainingRecordResponse>> page(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(trainingRecordService.page(studentId, courseId, startDate, endDate, page, size)));
    }
}
