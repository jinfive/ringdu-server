package com.ringdu.server.academy.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.dto.AcademyUpdateRequest;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcademyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ACADEMY는 내 학원 정보를 조회할 수 있다")
    void academyCanGetMyAcademy() throws Exception {
        String token = approvedAcademyAccessToken("controller-get-my-academy@ringdu.com");

        mockMvc.perform(get("/api/academies/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("링듀수학학원"))
                .andExpect(jsonPath("$.data.representativeName").value("홍길동"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN", "TEACHER", "PARENT", "STUDENT"})
    @DisplayName("ADMIN/TEACHER/PARENT/STUDENT는 내 학원 정보 API에 접근할 수 없다")
    void nonAcademyCannotGetMyAcademy(Role role) throws Exception {
        String token = accessToken(saveUser(role.name().toLowerCase() + "-get-my-academy@ringdu.com", role));

        mockMvc.perform(get("/api/academies/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비로그인 사용자는 내 학원 정보 API에 접근할 수 없다")
    void anonymousCannotGetMyAcademy() throws Exception {
        mockMvc.perform(get("/api/academies/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ACADEMY는 내 학원 정보를 수정할 수 있다")
    void academyCanUpdateMyAcademy() throws Exception {
        String email = "controller-update-my-academy@ringdu.com";
        String token = approvedAcademyAccessToken(email);

        mockMvc.perform(put("/api/academies/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyUpdateRequest(
                                "수정 학원",
                                "김대표",
                                "010-9999-8888",
                                "12345",
                                "서울시 서초구",
                                "3층"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 학원"));

        mockMvc.perform(get("/api/academies/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 학원"))
                .andExpect(jsonPath("$.data.representativeName").value("김대표"))
                .andExpect(jsonPath("$.data.detailAddress").value("3층"));
    }

    @Test
    @DisplayName("ACADEMY는 대시보드 요약을 조회할 수 있다")
    void academyCanGetDashboard() throws Exception {
        String token = approvedAcademyAccessToken("controller-dashboard@ringdu.com");

        mockMvc.perform(get("/api/academies/me/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentCount").value(0))
                .andExpect(jsonPath("$.data.teacherCount").value(0))
                .andExpect(jsonPath("$.data.unpaidInvoiceCount").value(0))
                .andExpect(jsonPath("$.data.pendingConsultationCount").value(0))
                .andExpect(jsonPath("$.data.notifications[0].type").value("INFO"))
                .andExpect(jsonPath("$.data", not(hasKey("classCount"))))
                .andExpect(jsonPath("$.data", not(hasKey("todayAttendanceCount"))))
                .andExpect(jsonPath("$.data", not(hasKey("pendingAttendanceApprovalCount"))));
    }

    private String approvedAcademyAccessToken(String email) {
        var signupResponse = authService.signupAcademy(academySignupRequest(email));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        return accessToken(userRepository.findByEmail(email).orElseThrow());
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
