package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.service.CoachService;
import com.shuzi.managementplatform.web.dto.coach.CoachCreateRequest;
import com.shuzi.managementplatform.web.dto.coach.CoachResponse;
import com.shuzi.managementplatform.web.dto.coach.CoachUpdateRequest;
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

import java.util.List;

/**
 * Coach management REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/coaches")
@Tag(name = "Coach", description = "Coach management endpoints")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create coach", description = "Create a new coach profile")
    public ApiResponse<CoachResponse> create(@Valid @RequestBody CoachCreateRequest request) {
        return ApiResponse.ok("coach created", coachService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update coach", description = "Update coach by ID")
    public ApiResponse<CoachResponse> update(@PathVariable Long id, @Valid @RequestBody CoachUpdateRequest request) {
        return ApiResponse.ok("coach updated", coachService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coach", description = "Delete coach by ID when it is not referenced by courses")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        coachService.delete(id);
        return ApiResponse.ok("coach deleted", null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get coach detail", description = "Fetch coach by ID")
    public ApiResponse<CoachResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(coachService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List coaches", description = "Paginated query by optional name and status")
    public ApiResponse<PageResponse<CoachResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CoachStatus status
    ) {
        return ApiResponse.ok(PageResponse.from(coachService.page(name, status, page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/options")
    @Operation(summary = "List active coach options", description = "List active coaches for selection controls")
    public ApiResponse<List<CoachResponse>> options() {
        return ApiResponse.ok(coachService.listActive());
    }
}
