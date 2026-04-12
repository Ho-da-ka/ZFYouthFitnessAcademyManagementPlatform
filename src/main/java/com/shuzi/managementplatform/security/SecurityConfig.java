package com.shuzi.managementplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration based on JWT Bearer token with endpoint-level RBAC rules.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/public/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        // Admin-only management operations
                        .requestMatchers(HttpMethod.POST, "/api/v1/students/**", "/api/v1/coaches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/**", "/api/v1/coaches/**").hasRole("ADMIN")
                        // Course create/update now allowed for admin and coach
                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/**").hasAnyRole("ADMIN", "COACH")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**").hasAnyRole("ADMIN", "COACH")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**", "/api/v1/coaches/**").hasRole("ADMIN")
                        // Admin and coach query operations
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/**", "/api/v1/courses/**", "/api/v1/coaches/**")
                        .hasAnyRole("ADMIN", "COACH")
                        // Coach and admin domain operations
                        .requestMatchers("/api/v1/attendances/**", "/api/v1/fitness-tests/**", "/api/v1/training-records/**")
                        .hasAnyRole("ADMIN", "COACH")
                        // Student self-service endpoints
                        .requestMatchers("/api/v1/student/**").hasRole("STUDENT")
                        // Reserved role boundary for phase-2 endpoints
                        .requestMatchers("/api/v1/parent/**").hasRole("PARENT")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
