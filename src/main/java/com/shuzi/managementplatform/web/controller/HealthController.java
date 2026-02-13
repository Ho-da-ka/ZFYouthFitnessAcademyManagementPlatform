package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Public non-authenticated endpoints.
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public", description = "Public health endpoints")
public class HealthController {

    @GetMapping("/ping")
    @Operation(summary = "Health check", description = "Return service status and current server time")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.ok(Map.of(
                "service", "management-platform",
                "time", LocalDateTime.now().toString(),
                "status", "UP"
        ));
    }
}
