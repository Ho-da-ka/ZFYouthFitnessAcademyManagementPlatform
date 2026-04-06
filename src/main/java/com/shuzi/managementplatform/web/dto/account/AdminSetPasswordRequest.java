package com.shuzi.managementplatform.web.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin set password request.
 */
public record AdminSetPasswordRequest(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "newPassword is required")
        @Size(min = 6, max = 64, message = "newPassword length must be between 6 and 64")
        String newPassword
) {
}
