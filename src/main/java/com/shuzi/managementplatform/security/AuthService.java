package com.shuzi.managementplatform.security;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.shuzi.managementplatform.domain.service.UserAccountService;
import com.shuzi.managementplatform.web.dto.auth.AuthTokenResponse;
import com.shuzi.managementplatform.web.dto.auth.LoginRequest;
import com.shuzi.managementplatform.web.dto.auth.RefreshTokenRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Provides login, refresh and logout capability based on JWT.
 */
@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserAccountService userAccountService;

    public AuthService(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenStore refreshTokenStore,
            UserAccountService userAccountService
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.userAccountService = userAccountService;
    }

    public AuthTokenResponse login(LoginRequest request) {
        String loginUsername = request.username().trim().toLowerCase(Locale.ROOT);
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginUsername);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        String role = extractRole(userDetails);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername(), role);
        RefreshTokenSession refreshTokenSession = refreshTokenStore.issue(userDetails.getUsername(), role);
        userAccountService.markLoginSuccess(userDetails.getUsername());

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTokenExpireSeconds(),
                refreshTokenSession.token(),
                userDetails.getUsername(),
                role
        );
    }

    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        RefreshTokenSession oldSession = refreshTokenStore.consume(request.refreshToken());
        if (oldSession == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "refreshToken 已失效，请重新登录");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(oldSession.username(), oldSession.role());
        RefreshTokenSession newSession = refreshTokenStore.issue(oldSession.username(), oldSession.role());

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTokenExpireSeconds(),
                newSession.token(),
                oldSession.username(),
                oldSession.role()
        );
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.revoke(refreshToken);
        }
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        userAccountService.changePasswordBySelf(username, oldPassword, newPassword);
    }

    private String extractRole(UserDetails userDetails) {
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            String value = authority.getAuthority();
            if (value != null && value.startsWith("ROLE_")) {
                return value.substring("ROLE_".length());
            }
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "用户未配置角色");
    }
}
