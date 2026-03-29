package com.shuzi.managementplatform.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request payload.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken is required") String refreshToken
) {
}
