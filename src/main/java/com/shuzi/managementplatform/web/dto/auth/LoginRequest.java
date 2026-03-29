package com.shuzi.managementplatform.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Username/password login request.
 */
public record LoginRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password
) {
}
