package com.ringdu.server.auth.controller;

import com.ringdu.server.auth.dto.LoginRequest;
import com.ringdu.server.auth.dto.LoginResponse;
import com.ringdu.server.auth.dto.LoginResult;
import com.ringdu.server.auth.dto.MeResponse;
import com.ringdu.server.auth.dto.SignupRequest;
import com.ringdu.server.auth.dto.SignupResponse;
import com.ringdu.server.auth.dto.TokenRefreshResponse;
import com.ringdu.server.auth.dto.TokenRefreshResult;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.global.security.cookie.RefreshTokenCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ApiResponse.success("회원가입이 완료되었습니다.", response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.createCookie(result.refreshToken()).toString())
                .body(ApiResponse.success("로그인이 완료되었습니다.", result.loginResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(HttpServletRequest request) {
        String refreshToken = refreshTokenCookieProvider.extractRefreshToken(request);
        TokenRefreshResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.createCookie(result.refreshToken()).toString())
                .body(ApiResponse.success("토큰이 재발급되었습니다.", result.tokenRefreshResponse()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String refreshToken = refreshTokenCookieProvider.extractRefreshToken(request);
        authService.logout(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.deleteCookie().toString())
                .body(ApiResponse.success("로그아웃되었습니다.", null));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        MeResponse response = authService.getMe(principal.userId());
        return ApiResponse.success(response);
    }
}
