package com.ringdu.server.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAcademySignupApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ADMIN은 승인 대기 학원 가입 신청 목록을 조회할 수 있다")
    void adminCanFindPendingApplications() throws Exception {
        authService.signupAcademy(academySignupRequest("controller-pending-list@ringdu.com"));
        String token = accessToken(saveUser("admin-pending-list@ringdu.com", Role.ADMIN));

        mockMvc.perform(get("/api/admin/academy-signup-applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("비로그인 사용자는 승인 대기 학원 가입 신청 목록을 조회할 수 없다")
    void anonymousCannotFindPendingApplications() throws Exception {
        mockMvc.perform(get("/api/admin/academy-signup-applications"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ACADEMY", "TEACHER", "PARENT", "STUDENT"})
    @DisplayName("ADMIN이 아닌 사용자는 승인 대기 학원 가입 신청 목록을 조회할 수 없다")
    void nonAdminCannotFindPendingApplications(Role role) throws Exception {
        String token = accessToken(saveUser(role.name().toLowerCase() + "-pending-list@ringdu.com", role));

        mockMvc.perform(get("/api/admin/academy-signup-applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN은 학원 가입 신청을 승인할 수 있다")
    void adminCanApproveApplication() throws Exception {
        var signupResponse = authService.signupAcademy(academySignupRequest("controller-approve@ringdu.com"));
        String token = accessToken(saveUser("admin-approve@ringdu.com", Role.ADMIN));

        mockMvc.perform(post("/api/admin/academy-signup-applications/{applicationId}/approve", signupResponse.applicationId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("controller-approve@ringdu.com"))
                .andExpect(jsonPath("$.data.userStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.applicationStatus").value("APPROVED"));
    }

    private String accessToken(User user) {
        return jwtTokenProvider.createAccessToken(user);
    }

    private User saveUser(String email, Role role) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 사용자",
                "010-1234-5678",
                role
        ));
    }

    private AcademySignupRequest academySignupRequest(String email) {
        return new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀수학학원",
                "홍길동",
                "010-1234-5678",
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        );
    }
}
