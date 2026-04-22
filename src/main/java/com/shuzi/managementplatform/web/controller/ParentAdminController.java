package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.common.api.PageResponse;
import com.shuzi.managementplatform.domain.service.ParentAdminService;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminDetailResponse;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentAdminListItemResponse;
import com.shuzi.managementplatform.web.dto.parentadmin.ParentManualBindingCreateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parents")
@PreAuthorize("hasRole('ADMIN')")
public class ParentAdminController {

    private final ParentAdminService parentAdminService;

    public ParentAdminController(ParentAdminService parentAdminService) {
        this.parentAdminService = parentAdminService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ParentAdminListItemResponse>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String studentName
    ) {
        return ApiResponse.ok(PageResponse.from(parentAdminService.page(keyword, studentName, page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ParentAdminDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(parentAdminService.getDetail(id));
    }

    @PostMapping("/manual-bindings")
    public ApiResponse<Void> addManualBinding(@Valid @RequestBody ParentManualBindingCreateRequest request) {
        parentAdminService.addManualBinding(request.parentId(), request.studentId());
        return ApiResponse.ok("parent binding created", null);
    }

    @DeleteMapping("/{id}/students/{studentId}/manual-binding")
    public ApiResponse<Void> removeManualBinding(@PathVariable Long id, @PathVariable Long studentId) {
        parentAdminService.removeManualBinding(id, studentId);
        return ApiResponse.ok("parent binding updated", null);
    }
}
