package com.ringdu.server.academy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.dto.TeacherInvitationCreateRequest;
import com.ringdu.server.academy.service.AcademyTeacherInvitationService;
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
class AcademyTeacherInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private AcademyTeacherInvitationService invitationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ACADEMY는 선생님 초대장을 생성할 수 있다")
    void academyCanCreateInvitation() throws Exception {
        String token = approvedAcademyAccessToken("controller-invite-academy@ringdu.com");

        mockMvc.perform(post("/api/academies/me/teacher-invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationRequest("controller-teacher@ringdu.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teacherEmail").value("controller-teacher@ringdu.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"TEACHER", "PARENT", "STUDENT"})
    @DisplayName("TEACHER/PARENT/STUDENT는 선생님 초대장을 생성할 수 없다")
    void nonAcademyCannotCreateInvitation(Role role) throws Exception {
        String token = accessToken(saveUser(role.name().toLowerCase() + "-cannot-invite@ringdu.com", role));

        mockMvc.perform(post("/api/academies/me/teacher-invitations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationRequest("blocked-teacher@ringdu.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ACADEMY는 자기 학원의 초대장 목록을 조회할 수 있다")
    void academyCanGetInvitations() throws Exception {
        String email = "controller-list-invitation-academy@ringdu.com";
        String token = approvedAcademyAccessToken(email);
        Long academyUserId = userRepository.findByEmail(email).orElseThrow().getId();
        invitationService.createInvitation(academyUserId, invitationRequest("controller-list-teacher@ringdu.com"));

        mockMvc.perform(get("/api/academies/me/teacher-invitations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].teacherEmail").value("controller-list-teacher@ringdu.com"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("ACADEMY는 자기 학원의 소속 선생님 목록을 조회할 수 있다")
    void academyCanGetTeachers() throws Exception {
        String email = "controller-list-teacher-academy@ringdu.com";
        String token = approvedAcademyAccessToken(email);
        Long academyUserId = userRepository.findByEmail(email).orElseThrow().getId();
        User teacher = saveUser("controller-connected-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();
        invitationService.acceptInvitation(teacher.getId(), invitationId);

        mockMvc.perform(get("/api/academies/me/teachers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].teacherUserId").value(teacher.getId()))
                .andExpect(jsonPath("$.data[0].email").value(teacher.getEmail()))
                .andExpect(jsonPath("$.data[0].memberStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("TEACHER는 자기 초대장 목록을 조회할 수 있다")
    void teacherCanGetOwnInvitations() throws Exception {
        Long academyUserId = approvedAcademyUserId("controller-teacher-own-list-academy@ringdu.com");
        User teacher = saveUser("controller-own-invitation-teacher@ringdu.com", Role.TEACHER);
        invitationService.createInvitation(academyUserId, invitationRequest(teacher.getEmail()));

        mockMvc.perform(get("/api/teacher/invitations")
                        .header("Authorization", "Bearer " + accessToken(teacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].teacherEmail").value(teacher.getEmail()))
                .andExpect(jsonPath("$.data[0].academyName").value("링듀수학학원"));
    }

    @Test
    @DisplayName("TEACHER는 자기 초대장을 수락할 수 있다")
    void teacherCanAcceptInvitation() throws Exception {
        Long academyUserId = approvedAcademyUserId("controller-accept-academy@ringdu.com");
        User teacher = saveUser("controller-accept-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();

        mockMvc.perform(post("/api/teacher/invitations/{invitationId}/accept", invitationId)
                        .header("Authorization", "Bearer " + accessToken(teacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("TEACHER는 자기 초대장을 거절할 수 있다")
    void teacherCanRejectInvitation() throws Exception {
        Long academyUserId = approvedAcademyUserId("controller-reject-academy@ringdu.com");
        User teacher = saveUser("controller-reject-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();

        mockMvc.perform(post("/api/teacher/invitations/{invitationId}/reject", invitationId)
                        .header("Authorization", "Bearer " + accessToken(teacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    private String approvedAcademyAccessToken(String email) {
        return accessToken(userRepository.findById(approvedAcademyUserId(email)).orElseThrow());
    }

    private Long approvedAcademyUserId(String email) {
        var signupResponse = authService.signupAcademy(academySignupRequest(email));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        return userRepository.findByEmail(email).orElseThrow().getId();
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

    private TeacherInvitationCreateRequest invitationRequest(String teacherEmail) {
        return new TeacherInvitationCreateRequest(
                teacherEmail,
                "010-2222-3333",
                "링듀수학학원에 함께해 주세요."
        );
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
