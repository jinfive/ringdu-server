package com.ringdu.server.auth.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.auth.dto.LoginRequest;
import com.ringdu.server.global.security.cookie.RefreshTokenCookieProvider;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("로컬 프론트 origin의 로그인 preflight 요청은 CORS 헤더와 함께 허용된다")
    void loginPreflightAllowedFromLocalFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3002")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3002"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("로그인 성공 시 accessToken과 refreshToken Cookie가 반환된다")
    void loginSuccess() throws Exception {
        saveUser("controller-login@ringdu.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "controller-login@ringdu.com",
                                "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(cookie().httpOnly(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME, true))
                .andExpect(cookie().value(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME, notNullValue()));
    }

    @Test
    @DisplayName("Refresh 성공 시 새 accessToken과 새 refreshToken Cookie가 반환된다")
    void refreshSuccess() throws Exception {
        saveUser("controller-refresh@ringdu.com");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "controller-refresh@ringdu.com",
                                "password123"
                        ))))
                .andReturn();
        Cookie oldCookie = loginResult.getResponse()
                .getCookie(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME);

        mockMvc.perform(post("/api/auth/refresh").cookie(oldCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(notNullValue()))
                .andExpect(cookie().value(
                        RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME,
                        not(oldCookie.getValue())
                ));
    }

    @Test
    @DisplayName("로그아웃은 인증 헤더 없이 refreshToken Cookie만으로 성공하고 Cookie를 만료한다")
    void logoutSuccessWithoutAuthorizationHeader() throws Exception {
        saveUser("controller-logout@ringdu.com");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                "controller-logout@ringdu.com",
                                "password123"
                        ))))
                .andReturn();
        Cookie refreshTokenCookie = loginResult.getResponse()
                .getCookie(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME);

        mockMvc.perform(post("/api/auth/logout").cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME, 0));

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("refreshToken Cookie가 없어도 로그아웃은 Cookie 만료 응답을 반환한다")
    void logoutSuccessWithoutRefreshTokenCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(RefreshTokenCookieProvider.REFRESH_TOKEN_COOKIE_NAME, 0));
    }

    @Test
    @DisplayName("내 정보 조회는 인증 없이 실패한다")
    void meFailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("내 정보 조회는 Access Token으로 성공한다")
    void meSuccessWithAccessToken() throws Exception {
        User user = saveUser("controller-me@ringdu.com");
        String accessToken = jwtTokenProvider.createAccessToken(user);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("controller-me@ringdu.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    private User saveUser(String email) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password123"),
                "홍길동",
                "010-1234-5678",
                Role.STUDENT
        ));
    }
}
