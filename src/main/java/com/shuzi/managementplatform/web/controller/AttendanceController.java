package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.AttendanceService;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceCreateRequest;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Attendance record endpoints.
 */
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
    @Operation(summary = "Create attendance record", description = "Record student attendance for a course")
    public ApiResponse<AttendanceResponse> create(@Valid @RequestBody AttendanceCreateRequest request) {
        return ApiResponse.ok("attendance recorded", attendanceService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "Search attendance", description = "Query by student, course and date range")
    public ApiResponse<List<AttendanceResponse>> search(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return ApiResponse.ok(attendanceService.search(studentId, courseId, startDate, endDate));
    }
}
