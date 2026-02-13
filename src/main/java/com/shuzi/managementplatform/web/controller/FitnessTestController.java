package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.FitnessTestService;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestCreateRequest;
import com.shuzi.managementplatform.web.dto.fitness.FitnessTestResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/fitness-tests")
@Tag(name = "FitnessTest", description = "体能测试记录接口")
public class FitnessTestController {

    private final FitnessTestService fitnessTestService;

    public FitnessTestController(FitnessTestService fitnessTestService) {
        this.fitnessTestService = fitnessTestService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @PostMapping
    @Operation(summary = "新增体测记录", description = "录入学员体能测试结果")
    public ApiResponse<FitnessTestResponse> create(@Valid @RequestBody FitnessTestCreateRequest request) {
        return ApiResponse.ok("fitness test recorded", fitnessTestService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @GetMapping
    @Operation(summary = "查询学员体测时间线", description = "按学员ID获取体测记录（时间倒序）")
    public ApiResponse<List<FitnessTestResponse>> listByStudent(@RequestParam Long studentId) {
        return ApiResponse.ok(fitnessTestService.listByStudent(studentId));
    }
}
