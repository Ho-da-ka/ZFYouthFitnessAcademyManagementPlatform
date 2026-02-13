package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import com.shuzi.managementplatform.domain.service.StudentService;
import com.shuzi.managementplatform.web.dto.student.StudentCreateRequest;
import com.shuzi.managementplatform.web.dto.student.StudentResponse;
import com.shuzi.managementplatform.web.dto.student.StudentUpdateRequest;
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

/**
 * Student management REST endpoints.
 */
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student", description = "Student profile management endpoints")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create student", description = "Create a new student profile")
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        return ApiResponse.ok("student created", studentService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update student", description = "Update student profile by ID")
    public ApiResponse<StudentResponse> update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ApiResponse.ok("student updated", studentService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping("/{id}")
    @Operation(summary = "Get student detail", description = "Fetch student profile by ID")
    public ApiResponse<StudentResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List students", description = "Paginated query by optional name and status")
    public ApiResponse<PageResponse<StudentResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StudentStatus status
    ) {
        return ApiResponse.ok(PageResponse.from(studentService.page(name, status, page, size)));
    }
}
