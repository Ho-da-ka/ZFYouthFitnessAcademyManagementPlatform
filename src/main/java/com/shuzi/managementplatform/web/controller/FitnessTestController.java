package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.service.FitnessTestService;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestUpdateRequest;
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

@RestController
@RequestMapping("/api/v1/fitness-tests")
@Tag(name = "FitnessTest", description = "Fitness assessment endpoints")
public class FitnessTestController {

    private final FitnessTestService fitnessTestService;

    public FitnessTestController(FitnessTestService fitnessTestService) {
        this.fitnessTestService = fitnessTestService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "Create fitness test record")
    public ApiResponse<FitnessTestResponse> create(@Valid @RequestBody FitnessTestCreateRequest request) {
        return ApiResponse.ok("fitness test recorded", fitnessTestService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PutMapping("/{id}")
    @Operation(summary = "Update fitness test record")
    public ApiResponse<FitnessTestResponse> update(@PathVariable Long id, @Valid @RequestBody FitnessTestUpdateRequest request) {
        return ApiResponse.ok("fitness test updated", fitnessTestService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete fitness test record")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fitnessTestService.delete(id);
        return ApiResponse.ok("fitness test deleted", null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List fitness tests with pagination")
    public ApiResponse<PageResponse<FitnessTestResponse>> page(
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(fitnessTestService.page(studentId, page, size)));
    }
}

