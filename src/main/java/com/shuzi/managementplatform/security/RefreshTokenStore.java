package com.shuzi.managementplatform.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.RefreshTokenEntity;
import com.shuzi.managementplatform.domain.mapper.RefreshTokenMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Database-backed refresh token storage.
 */
@Component
public class RefreshTokenStore {

    private final long refreshTokenExpireSeconds;
    private final RefreshTokenMapper refreshTokenMapper;

    public RefreshTokenStore(
            @Value("${security.jwt.refresh-token-expire-seconds:1209600}") long refreshTokenExpireSeconds,
            RefreshTokenMapper refreshTokenMapper
    ) {
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Transactional
    public RefreshTokenSession issue(String username, String role) {
        cleanupExpired();
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(refreshTokenExpireSeconds);

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setToken(token);
        entity.setUsername(username);
        entity.setRole(role);
        entity.setExpiresAt(toLocalDateTime(expiresAt));
        refreshTokenMapper.insert(entity);
        return new RefreshTokenSession(token, username, role, expiresAt);
    }

    @Transactional
    public RefreshTokenSession consume(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        cleanupExpired();
        RefreshTokenEntity entity = refreshTokenMapper.selectOne(
                Wrappers.<RefreshTokenEntity>lambdaQuery()
                        .eq(RefreshTokenEntity::getToken, token)
        );
        if (entity == null) {
            return null;
        }
        Instant expiresAt = toInstant(entity.getExpiresAt());
        if (expiresAt.isBefore(Instant.now())) {
            refreshTokenMapper.delete(Wrappers.<RefreshTokenEntity>lambdaQuery().eq(RefreshTokenEntity::getToken, token));
            return null;
        }
        int deleted = refreshTokenMapper.delete(
                Wrappers.<RefreshTokenEntity>lambdaQuery().eq(RefreshTokenEntity::getToken, token)
        );
        if (deleted <= 0) {
            return null;
        }
        return new RefreshTokenSession(entity.getToken(), entity.getUsername(), entity.getRole(), expiresAt);
    }

    @Transactional
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        refreshTokenMapper.delete(
                Wrappers.<RefreshTokenEntity>lambdaQuery().eq(RefreshTokenEntity::getToken, token)
        );
    }

    @Transactional
    private void cleanupExpired() {
        refreshTokenMapper.delete(
                Wrappers.<RefreshTokenEntity>lambdaQuery()
                        .lt(RefreshTokenEntity::getExpiresAt, LocalDateTime.now())
        );
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
