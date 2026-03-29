package com.shuzi.managementplatform.security;

import java.time.Instant;

/**
 * Server-side refresh token session metadata.
 */
public record RefreshTokenSession(
        String token,
        String username,
        String role,
        Instant expiresAt
) {
}
