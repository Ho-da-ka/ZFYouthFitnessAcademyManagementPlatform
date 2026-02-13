package com.shuzi.managementplatform.security;

import org.springframework.stereotype.Component;

/**
 * Placeholder JWT validator.
 * Current implementation only checks token non-blank and is intended to be replaced later.
 */
@Component
public class JwtTokenProvider {

    public boolean validate(String token) {
        return token != null && !token.isBlank();
    }
}
