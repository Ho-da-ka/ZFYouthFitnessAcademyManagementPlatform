package com.shuzi.managementplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;

/**
 * Generates and validates JWT access tokens.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String ACCESS_TYPE = "access";

    private final SecretKey signingKey;
    private final long accessTokenExpireSeconds;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expire-seconds:7200}") long accessTokenExpireSeconds
    ) {
        this.signingKey = buildSigningKey(secret);
        this.accessTokenExpireSeconds = accessTokenExpireSeconds;
    }

    public String generateAccessToken(String username, String role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpireSeconds);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, ACCESS_TYPE)
                .signWith(signingKey)
                .compact();
    }

    public JwtPrincipal parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get(CLAIM_TYPE, String.class);
        if (!ACCESS_TYPE.equals(type)) {
            throw new JwtException("Unsupported token type");
        }

        String username = claims.getSubject();
        String role = claims.get(CLAIM_ROLE, String.class);
        if (username == null || username.isBlank() || role == null || role.isBlank()) {
            throw new JwtException("Token payload is incomplete");
        }
        return new JwtPrincipal(username, role);
    }

    public long getAccessTokenExpireSeconds() {
        return accessTokenExpireSeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey buildSigningKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn("[SECURITY] JWT secret is shorter than 32 bytes and has been hashed. Please configure a strong secret in production.");
            try {
                bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to build signing key", ex);
            }
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
