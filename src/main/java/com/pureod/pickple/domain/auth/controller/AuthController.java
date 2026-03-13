package com.pureod.pickple.domain.auth.controller;

import com.pureod.pickple.domain.auth.dto.JwtDto;
import com.pureod.pickple.domain.auth.dto.ResetPasswordRequest;
import com.pureod.pickple.domain.auth.dto.SignInRequest;
import com.pureod.pickple.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 관리", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 - SecurityFilterChain에서 처리 (Swagger 문서용)
     * 실제 요청은 UsernamePasswordAuthenticationFilter가 가로채서 처리합니다.
     */
    @Operation(
        summary = "로그인",
        description = "SecurityFilterChain에서 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = JwtDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 오류")
    })
    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<JwtDto> signIn(SignInRequest request) {
        // 이 메서드는 실제로 호출되지 않습니다.
        // UsernamePasswordAuthenticationFilter가 /api/auth/sign-in 요청을 가로챕니다.
        throw new UnsupportedOperationException("이 엔드포인트는 SecurityFilterChain에서 처리됩니다.");
    }

    /**
     * 로그아웃 - SecurityFilterChain에서 처리 (Swagger 문서용)
     * 실제 요청은 LogoutFilter가 가로채서 처리합니다.
     */
    @Operation(
        summary = "로그아웃",
        description = "SecurityFilterChain에서 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "성공")
    })
    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut() {
        // 이 메서드는 실제로 호출되지 않습니다.
        // LogoutFilter가 /api/auth/sign-out 요청을 가로챕니다.
        throw new UnsupportedOperationException("이 엔드포인트는 SecurityFilterChain에서 처리됩니다.");
    }

    /**
     * 토큰 재발급 - 쿠키의 REFRESH_TOKEN으로 새 Access/Refresh 토큰 발급
     */
    @Operation(
        summary = "토큰 재발급",
        description = "쿠키(REFRESH_TOKEN)에 저장된 리프레시 토큰으로 리프레시 토큰과 엑세스 토큰을 재발급합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = JwtDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> reissueToken(
        HttpServletRequest request,
        HttpServletResponse response) {
        return ResponseEntity.ok(authService.reissueToken(request, response));
    }

    /**
     * CSRF 토큰 조회 - CookieCsrfTokenRepository가 자동으로 쿠키에 저장
     */
    @Operation(
        summary = "CSRF 토큰 조회",
        description = "CSRF 토큰을 조회합니다. 토큰은 쿠키(XSRF-TOKEN)에 저장됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "성공")
    })
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken() {
        return ResponseEntity.noContent().build();
    }

    /**
     * 비밀번호 초기화 - 임시 비밀번호 발급 후 이메일 전송
     */
    @Operation(
        summary = "비밀번호 초기화",
        description = "임시 비밀번호로 초기화 후 이메일로 전송합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "성공"),
        @ApiResponse(responseCode = "404", description = "해당 리소스 없음")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.email());
        return ResponseEntity.noContent().build();
    }
}