
package com.pureod.pickple.global.security;

import com.nimbusds.jose.JOSEException;
import com.pureod.pickple.domain.auth.dto.JwtDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        try {
            PickpleUserDetails userDetails = (PickpleUserDetails) authentication.getPrincipal();

            // Access Token 생성
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

            // Refresh Token 생성
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // Refresh Token을 HttpOnly 쿠키로 설정
            response.addCookie(jwtTokenProvider.generateRefreshTokenCookie(refreshToken));

            // Access Token + UserDto를 JSON 응답으로 반환
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            JwtDto jwtDto = new JwtDto(accessToken, userDetails.getUserDto());
            objectMapper.writeValue(response.getWriter(), jwtDto);

            log.info("로그인 성공: {}", userDetails.getUsername());
        } catch (JOSEException e) {
            log.error("JWT 토큰 생성 실패", e);
            throw new ServletException("JWT 토큰 생성 실패", e);
        }
    }
}
