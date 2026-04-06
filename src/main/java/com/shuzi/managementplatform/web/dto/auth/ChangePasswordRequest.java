package com.shuzi.managementplatform.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-service password change request.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "oldPassword is required")
        String oldPassword,

        @NotBlank(message = "newPassword is required")
        @Size(min = 6, max = 64, message = "newPassword length must be between 6 and 64")
        String newPassword
) {
}
