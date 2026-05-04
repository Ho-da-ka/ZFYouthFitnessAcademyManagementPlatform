package com.shuzi.managementplatform.security;

import com.shuzi.managementplatform.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuzi.managementplatform.domain.entity.Coach;
import com.shuzi.managementplatform.domain.entity.Student;
import com.shuzi.managementplatform.domain.entity.UserAccount;
import com.shuzi.managementplatform.domain.mapper.CoachMapper;
import com.shuzi.managementplatform.domain.mapper.StudentMapper;
import com.shuzi.managementplatform.domain.mapper.UserAccountMapper;
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
import org.springframework.util.StringUtils;

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
    private final LoginCryptoService loginCryptoService;
    private final UserAccountMapper userAccountMapper;
    private final CoachMapper coachMapper;
    private final StudentMapper studentMapper;

    public AuthService(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenStore refreshTokenStore,
            UserAccountService userAccountService,
            LoginCryptoService loginCryptoService,
            UserAccountMapper userAccountMapper,
            CoachMapper coachMapper,
            StudentMapper studentMapper
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.userAccountService = userAccountService;
        this.loginCryptoService = loginCryptoService;
        this.userAccountMapper = userAccountMapper;
        this.coachMapper = coachMapper;
        this.studentMapper = studentMapper;
    }

    public AuthTokenResponse login(LoginRequest request) {
        String loginUsername = request.username().trim().toLowerCase(Locale.ROOT);
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(loginUsername);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        String rawPassword = resolveRawPassword(request);
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        String role = extractRole(userDetails);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getUsername(), role);
        RefreshTokenSession refreshTokenSession = refreshTokenStore.issue(userDetails.getUsername(), role);
        userAccountService.markLoginSuccess(userDetails.getUsername());

        String nickname = resolveNickname(userDetails.getUsername());

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTokenExpireSeconds(),
                refreshTokenSession.token(),
                userDetails.getUsername(),
                nickname,
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

        String nickname = resolveNickname(oldSession.username());

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTokenExpireSeconds(),
                newSession.token(),
                oldSession.username(),
                nickname,
                oldSession.role()
        );
    }

    private String resolveNickname(String username) {
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery().eq(UserAccount::getUsername, username)
        );
        if (account == null) {
            return username;
        }

        if (account.getCoachId() != null) {
            Coach coach = coachMapper.selectById(account.getCoachId());
            if (coach != null && StringUtils.hasText(coach.getName())) {
                return coach.getName();
            }
        }

        if (account.getStudentId() != null) {
            Student student = studentMapper.selectById(account.getStudentId());
            if (student != null && StringUtils.hasText(student.getName())) {
                return student.getName();
            }
        }

        return username;
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.revoke(refreshToken);
        }
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        userAccountService.changePasswordBySelf(username, oldPassword, newPassword);
    }

    private String resolveRawPassword(LoginRequest request) {
        if (StringUtils.hasText(request.encryptedPassword()) || StringUtils.hasText(request.iv())) {
            if (!StringUtils.hasText(request.encryptedPassword()) || !StringUtils.hasText(request.iv())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "login credential is incomplete");
            }
            return loginCryptoService.decrypt(request.encryptedPassword(), request.iv());
        }
        if (!StringUtils.hasText(request.password())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "password is required");
        }
        return request.password();
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
