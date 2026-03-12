
package com.pureod.pickple.domain.auth.controller;

import com.pureod.pickple.domain.auth.dto.ResetPasswordRequest;
import com.pureod.pickple.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 토큰 재발급 - 쿠키의 REFRESH_TOKEN으로 새 Access/Refresh 토큰 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> reissueToken(
        HttpServletRequest request,
        HttpServletResponse response) {
        return ResponseEntity.ok(authService.reissueToken(request, response));
    }

    /**
     * CSRF 토큰 조회 - CookieCsrfTokenRepository가 자동으로 쿠키에 저장
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken() {
        return ResponseEntity.noContent().build();
    }

    /**
     * 비밀번호 초기화 - 임시 비밀번호 발급 후 이메일 전송
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
        @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email());
        return ResponseEntity.noContent().build();
    }

    // sign-in, sign-out은 SecurityFilterChain에서 처리 (Swagger 문서용)
}