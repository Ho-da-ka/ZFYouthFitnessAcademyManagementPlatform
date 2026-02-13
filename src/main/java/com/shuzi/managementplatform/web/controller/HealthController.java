package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public", description = "公共可访问接口")
public class HealthController {

    @GetMapping("/ping")
    @Operation(summary = "健康检查", description = "返回服务状态与当前时间")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.ok(Map.of(
                "service", "management-platform",
                "time", LocalDateTime.now().toString(),
                "status", "UP"
        ));
    }
}
