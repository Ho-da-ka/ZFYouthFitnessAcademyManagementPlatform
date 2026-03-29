package com.shuzi.managementplatform.web.dto.auth;

/**
 * Token response returned from login/refresh endpoints.
 */
public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        String username,
        String role
) {
}
