package com.shuzi.managementplatform.web.controller;

import com.shuzi.managementplatform.common.api.ApiResponse;
import com.shuzi.managementplatform.security.AuthService;
import com.shuzi.managementplatform.web.dto.auth.AuthTokenResponse;
import com.shuzi.managementplatform.web.dto.auth.ChangePasswordRequest;
import com.shuzi.managementplatform.web.dto.auth.LoginRequest;
import com.shuzi.managementplatform.web.dto.auth.LogoutRequest;
import com.shuzi.managementplatform.web.dto.auth.RefreshTokenRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT auth endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        String refreshToken = request == null ? null : request.refreshToken();
        authService.logout(refreshToken);
        return ApiResponse.ok("Logout success", null);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        authService.changePassword(username, request.oldPassword(), request.newPassword());
        return ApiResponse.ok("password changed", null);
    }
}
