package com.shuzi.managementplatform.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public boolean validate(String token) {
        return token != null && !token.isBlank();
    }
}
