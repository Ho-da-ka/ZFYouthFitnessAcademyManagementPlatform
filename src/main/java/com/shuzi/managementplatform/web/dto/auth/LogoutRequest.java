package com.shuzi.managementplatform.web.dto.auth;

/**
 * Logout request payload.
 */
public record LogoutRequest(String refreshToken) {
}
