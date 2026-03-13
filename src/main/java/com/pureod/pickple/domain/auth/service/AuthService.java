package com.pureod.pickple.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.pureod.pickple.domain.auth.dto.JwtDto;
import com.pureod.pickple.domain.user.entity.User;
import com.pureod.pickple.domain.user.repository.UserRepository;
import com.pureod.pickple.global.exception.ResourceNotFoundException;
import com.pureod.pickple.global.security.JwtTokenProvider;
import com.pureod.pickple.global.security.PickpleUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JwtDto reissueToken(HttpServletRequest request,
        HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        PickpleUserDetails userDetails =
            (PickpleUserDetails) userDetailsService.loadUserByUsername(email);

        try {
            String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            response.addCookie(jwtTokenProvider.generateRefreshTokenCookie(newRefreshToken));

            return new JwtDto(newAccessToken, userDetails.getUserDto());
        } catch (JOSEException e) {
            throw new RuntimeException("토큰 생성 실패", e);
        }
    }

    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다."));

        String temporaryPassword = generateTemporaryPassword();
        user.changePassword(passwordEncoder.encode(temporaryPassword));

        // TODO: 이메일 발송 인프라 연결 후 임시 비밀번호 전송 처리
        log.info("임시 비밀번호 발급 완료. email={}, temporaryPassword={}", email, temporaryPassword);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
            .filter(c -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    private String generateTemporaryPassword() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder builder = new StringBuilder(TEMP_PASSWORD_LENGTH);

        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARS.length());
            builder.append(TEMP_PASSWORD_CHARS.charAt(index));
        }

        return builder.toString();
    }
}
