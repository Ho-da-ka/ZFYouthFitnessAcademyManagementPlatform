package com.shuzi.managementplatform.web.dto.account;

import jakarta.validation.constraints.NotBlank;

/**
 * Admin reset password request.
 */
public record AdminResetPasswordRequest(
        @NotBlank(message = "username is required")
        String username
) {
}
