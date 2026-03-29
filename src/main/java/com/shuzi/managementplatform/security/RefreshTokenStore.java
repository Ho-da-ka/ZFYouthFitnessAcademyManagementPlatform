package com.shuzi.managementplatform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory refresh token storage.
 */
@Component
public class RefreshTokenStore {

    private final long refreshTokenExpireSeconds;
    private final Map<String, RefreshTokenSession> sessions = new ConcurrentHashMap<>();

    public RefreshTokenStore(@Value("${security.jwt.refresh-token-expire-seconds:1209600}") long refreshTokenExpireSeconds) {
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }

    public RefreshTokenSession issue(String username, String role) {
        cleanupExpired();
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpireSeconds);
        RefreshTokenSession session = new RefreshTokenSession(token, username, role, expiresAt);
        sessions.put(token, session);
        return session;
    }

    public RefreshTokenSession consume(String token) {
        cleanupExpired();
        RefreshTokenSession session = sessions.remove(token);
        if (session == null) {
            return null;
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return session;
    }

    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        sessions.remove(token);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
