package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.enums.CourseStatus;
import com.shuzi.managementplatform.domain.service.CourseService;
import com.shuzi.managementplatform.web.dto.course.CourseAttendanceStatsResponse;
import com.shuzi.managementplatform.web.dto.course.CourseCreateRequest;
import com.shuzi.managementplatform.web.dto.course.CourseResponse;
import com.shuzi.managementplatform.web.dto.course.CourseStudentResponse;
import com.shuzi.managementplatform.web.dto.course.CourseUpdateRequest;
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
 * Course management REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course", description = "Course management endpoints")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "Create course", description = "Create a new course")
    public ApiResponse<CourseResponse> create(
            @Valid @RequestBody CourseCreateRequest request,
            @RequestParam(defaultValue = "false") boolean ignoreConflict
    ) {
        return ApiResponse.ok("course created", courseService.create(request, ignoreConflict));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PutMapping("/{id}")
    @Operation(summary = "Update course", description = "Update course by ID")
    public ApiResponse<CourseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            @RequestParam(defaultValue = "false") boolean ignoreConflict
    ) {
        return ApiResponse.ok("course updated", courseService.update(id, request, ignoreConflict));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        courseService.delete(id, force);
        return ApiResponse.ok("course deleted", null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get course detail", description = "Fetch course by ID")
    public ApiResponse<CourseResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List courses", description = "Paginated query by optional name and status")
    public ApiResponse<PageResponse<CourseResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CourseStatus status
    ) {
        return ApiResponse.ok(PageResponse.from(courseService.page(name, status, page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}/students")
    @Operation(summary = "Get enrolled students", description = "Get students with attendance stats for one course")
    public ApiResponse<List<CourseStudentResponse>> getStudents(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getCourseStudents(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}/attendance-stats")
    @Operation(summary = "Get course attendance stats", description = "Get summary and trend for one course")
    public ApiResponse<CourseAttendanceStatsResponse> getAttendanceStats(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getCourseAttendanceStats(id));
    }
}
