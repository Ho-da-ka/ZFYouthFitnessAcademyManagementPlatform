package com.shuzi.managementplatform.web.dto.account;

/**
 * Password reset response.
 */
public record ResetPasswordResponse(
        String username,
        String newPassword
) {
}
