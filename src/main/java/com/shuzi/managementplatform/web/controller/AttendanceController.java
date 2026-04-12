package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.service.AttendanceService;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceUpdateRequest;
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

@RestController
@RequestMapping("/api/v1/attendances")
@Tag(name = "Attendance", description = "Attendance record endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "Create attendance record")
    public ApiResponse<AttendanceResponse> create(@Valid @RequestBody AttendanceCreateRequest request) {
        return ApiResponse.ok("attendance recorded", attendanceService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PutMapping("/{id}")
    @Operation(summary = "Update attendance record")
    public ApiResponse<AttendanceResponse> update(@PathVariable Long id, @Valid @RequestBody AttendanceUpdateRequest request) {
        return ApiResponse.ok("attendance updated", attendanceService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List attendances with pagination")
    public ApiResponse<PageResponse<AttendanceResponse>> page(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(attendanceService.page(studentId, courseId, startDate, endDate, page, size)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance record")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ApiResponse.ok("attendance deleted", null);
    }
}
