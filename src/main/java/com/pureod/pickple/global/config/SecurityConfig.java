package com.pureod.pickple.global.config;

import com.pureod.pickple.global.security.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtLogoutHandler jwtLogoutHandler;
    private final Http403ForbiddenAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 세션 사용하지 않음 (JWT 기반)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // CSRF 설정 (api-docs에 csrfToken 보안 스키마 존재)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    "/api/auth/sign-in",
                    "/api/users"  // 회원가입
                )
            )

            // URL별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 불필요
                .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()           // 회원가입
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg").permitAll()

                // 어드민 전용
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers("/api/users/*/role").hasRole("ADMIN")
                .requestMatchers("/api/users/*/locked").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/contents").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/contents/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/contents/*").hasRole("ADMIN")

                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 로그인 (SecurityFilterChain에서 처리)
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/sign-in")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(jwtLoginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )

            // 로그아웃
            .logout(logout -> logout
                .logoutUrl("/api/auth/sign-out")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler((req, res, auth) ->
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT))
            )

            // 403 처리
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint((req, res, authEx) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write(
                        "{\"exceptionName\":\"AuthenticationException\",\"message\":\"인증이 필요합니다.\"}"
                    );
                })
            )

            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}