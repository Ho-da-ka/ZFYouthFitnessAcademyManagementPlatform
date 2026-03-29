package com.shuzi.managementplatform.security;

/**
 * Principal data extracted from an access token.
 */
public record JwtPrincipal(String username, String role) {
}
