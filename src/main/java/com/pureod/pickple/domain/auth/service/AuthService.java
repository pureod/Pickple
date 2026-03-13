package com.pureod.pickple.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.pureod.pickple.domain.auth.dto.JwtDto;
import com.pureod.pickple.global.security.JwtTokenProvider;
import com.pureod.pickple.global.security.PickpleUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtDto reissueToken(HttpServletRequest request,
        HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. Refresh Token에서 이메일 추출 → 사용자 조회
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        PickpleUserDetails userDetails =
            (PickpleUserDetails) userDetailsService.loadUserByUsername(email);

        try {
            // 3. 새 토큰 발급
            String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            // 4. 새 Refresh Token 쿠키 설정
            response.addCookie(jwtTokenProvider.generateRefreshTokenCookie(newRefreshToken));

            return new JwtDto(newAccessToken, userDetails.getUserDto());
        } catch (JOSEException e) {
            throw new RuntimeException("토큰 생성 실패", e);
        }
    }

    public void resetPassword(String email) {
        // TODO: 임시 비밀번호 생성 → 사용자 비밀번호 업데이트 → 이메일 발송
        throw new UnsupportedOperationException("비밀번호 초기화 기능 미구현");
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
            .filter(c -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}