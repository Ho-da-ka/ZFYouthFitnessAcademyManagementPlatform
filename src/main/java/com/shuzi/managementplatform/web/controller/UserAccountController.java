package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.domain.service.UserAccountService;
import com.shuzi.managementplatform.web.dto.account.AdminResetPasswordRequest;
import com.shuzi.managementplatform.web.dto.account.AdminSetPasswordRequest;
import com.shuzi.managementplatform.web.dto.account.ResetPasswordResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User account password management endpoints.
 */
@RestController
@RequestMapping("/api/v1/user-accounts")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/password")
    public ApiResponse<Void> adminSetPassword(@Valid @RequestBody AdminSetPasswordRequest request) {
        userAccountService.adminSetPassword(request.username(), request.newPassword());
        return ApiResponse.ok("password updated", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponse> adminResetPassword(@Valid @RequestBody AdminResetPasswordRequest request) {
        String newPassword = userAccountService.adminResetInitialPassword(request.username());
        return ApiResponse.ok("password reset", new ResetPasswordResponse(request.username(), newPassword));
    }
}
