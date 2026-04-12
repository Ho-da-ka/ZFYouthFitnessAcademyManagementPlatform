package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.CareAlertService;
import com.shuzi.managementplatform.web.dto.alert.CareAlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/care-alerts")
@Tag(name = "CareAlert", description = "Care alert endpoints")
public class CareAlertController {

    private final CareAlertService careAlertService;

    public CareAlertController(CareAlertService careAlertService) {
        this.careAlertService = careAlertService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "List care alerts")
    public ApiResponse<List<CareAlertResponse>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.ok(careAlertService.listAlerts(studentId, status, limit));
    }
}
