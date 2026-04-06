package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.ParentPortalService;
import com.shuzi.managementplatform.web.dto.parent.ParentBookingCreateRequest;
import com.shuzi.managementplatform.web.dto.parent.ParentBookingResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinCreateRequest;
import com.shuzi.managementplatform.web.dto.parent.ParentCheckinResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentChildResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentCourseResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentFitnessResponse;
import com.shuzi.managementplatform.web.dto.parent.ParentMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Parent-side mini-program portal endpoints.
 */
@RestController
@RequestMapping("/api/v1/parent")
@Tag(name = "ParentPortal", description = "Parent-side app endpoints")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    public ParentPortalController(ParentPortalService parentPortalService) {
        this.parentPortalService = parentPortalService;
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/children")
    @Operation(summary = "List bound children", description = "Get student profiles visible to current parent user")
    public ApiResponse<List<ParentChildResponse>> listChildren(Authentication authentication) {
        return ApiResponse.ok(parentPortalService.listChildren(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/courses")
    @Operation(summary = "List available courses", description = "Get parent-bookable courses with capacity summary")
    public ApiResponse<List<ParentCourseResponse>> listCourses(Authentication authentication) {
        return ApiResponse.ok(parentPortalService.listCourses(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/bookings")
    @Operation(summary = "List bookings", description = "Get booking records of current parent")
    public ApiResponse<List<ParentBookingResponse>> listBookings(Authentication authentication) {
        return ApiResponse.ok(parentPortalService.listBookings(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/bookings")
    @Operation(summary = "Create booking", description = "Create one booking for selected child and course")
    public ApiResponse<ParentBookingResponse> createBooking(
            Authentication authentication,
            @Valid @RequestBody ParentBookingCreateRequest request
    ) {
        return ApiResponse.ok("booking created", parentPortalService.createBooking(currentUsername(authentication), request));
    }

    @PreAuthorize("hasRole('PARENT')")
    @DeleteMapping("/bookings/{id}")
    @Operation(summary = "Cancel booking", description = "Cancel a parent booking by ID")
    public ApiResponse<ParentBookingResponse> cancelBooking(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("booking canceled", parentPortalService.cancelBooking(currentUsername(authentication), id));
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/checkins")
    @Operation(summary = "List check-ins", description = "List attendance records for current parent's children")
    public ApiResponse<List<ParentCheckinResponse>> listCheckins(Authentication authentication) {
        return ApiResponse.ok(parentPortalService.listCheckins(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/checkins")
    @Operation(summary = "Create check-in", description = "Parent manual check-in based on booking")
    public ApiResponse<ParentCheckinResponse> createCheckin(
            Authentication authentication,
            @Valid @RequestBody ParentCheckinCreateRequest request
    ) {
        return ApiResponse.ok("checkin created", parentPortalService.createCheckin(currentUsername(authentication), request));
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/fitness-tests")
    @Operation(summary = "List fitness tests", description = "List fitness test records for current parent's children")
    public ApiResponse<List<ParentFitnessResponse>> listFitness(
            Authentication authentication,
            @RequestParam(required = false) Long studentId
    ) {
        return ApiResponse.ok(parentPortalService.listFitnessTests(currentUsername(authentication), studentId));
    }

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping("/messages")
    @Operation(summary = "List messages", description = "List parent-side in-app messages")
    public ApiResponse<List<ParentMessageResponse>> listMessages(Authentication authentication) {
        return ApiResponse.ok(parentPortalService.listMessages(currentUsername(authentication)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PatchMapping("/messages/{id}/read")
    @Operation(summary = "Read message", description = "Mark one in-app message as read")
    public ApiResponse<ParentMessageResponse> readMessage(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("message read", parentPortalService.markMessageRead(currentUsername(authentication), id));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/messages/{id}/read")
    @Operation(summary = "Read message (POST compatible)", description = "Compatibility endpoint for clients without PATCH support")
    public ApiResponse<ParentMessageResponse> readMessageByPost(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("message read", parentPortalService.markMessageRead(currentUsername(authentication), id));
    }

    private String currentUsername(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
