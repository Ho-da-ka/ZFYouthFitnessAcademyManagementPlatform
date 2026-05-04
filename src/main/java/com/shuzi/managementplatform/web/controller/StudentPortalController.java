package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.StudentPortalService;
import com.shuzi.managementplatform.domain.service.CheckinService;
import com.shuzi.managementplatform.web.dto.attendance.AttendanceResponse;
import com.shuzi.managementplatform.web.dto.checkin.StudentScanCheckinRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
import com.shuzi.managementplatform.web.dto.student.StudentCourseResponse;
import com.shuzi.managementplatform.web.dto.student.StudentPortalProfileResponse;
import com.shuzi.managementplatform.web.dto.training.TrainingRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Student-side mini-program portal endpoints.
 */
@RestController
@RequestMapping("/api/v1/student")
@Tag(name = "StudentPortal", description = "Student-side app endpoints")
public class StudentPortalController {

    private final StudentPortalService studentPortalService;
    private final CheckinService checkinService;

    public StudentPortalController(StudentPortalService studentPortalService, CheckinService checkinService) {
        this.studentPortalService = studentPortalService;
        this.checkinService = checkinService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile")
    @Operation(summary = "Get student profile", description = "Get current student profile by login identity")
    public ApiResponse<StudentPortalProfileResponse> profile(Authentication authentication) {
        return ApiResponse.ok(studentPortalService.getProfile(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/courses")
    @Operation(summary = "List my courses", description = "List course records associated with current student")
    public ApiResponse<List<StudentCourseResponse>> courses(Authentication authentication) {
        return ApiResponse.ok(studentPortalService.listCourses(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/training-records")
    @Operation(summary = "List my training records", description = "List training records of current student")
    public ApiResponse<List<TrainingRecordResponse>> trainings(Authentication authentication) {
        return ApiResponse.ok(studentPortalService.listTrainingRecords(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/fitness-tests")
    @Operation(summary = "List my fitness tests", description = "List fitness test records of current student")
    public ApiResponse<List<FitnessTestResponse>> fitness(Authentication authentication) {
        return ApiResponse.ok(studentPortalService.listFitnessTests(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/scan-checkin")
    @Operation(summary = "Scan QR check-in", description = "Check-in by scanning coach-provided QR code")
    public ApiResponse<AttendanceResponse> scanCheckin(
            @Valid @RequestBody StudentScanCheckinRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok("签到成功", checkinService.studentScanCheckin(request.token(), currentUsername(authentication)));
    }

    private String currentUsername(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
