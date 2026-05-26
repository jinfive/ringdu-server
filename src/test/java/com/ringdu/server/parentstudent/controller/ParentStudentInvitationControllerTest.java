package com.ringdu.server.parentstudent.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationCreateRequest;
import com.ringdu.server.parentstudent.dto.StudentParentInvitationCreateRequest;
import com.ringdu.server.parentstudent.service.ParentStudentInvitationService;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class ParentStudentInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParentStudentInvitationService invitationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("PARENT는 STUDENT에게 연결 초대장을 보낼 수 있다")
    void parentCanCreateStudentInvitation() throws Exception {
        User parent = saveUser("controller-parent-invite@ringdu.com", Role.PARENT);

        mockMvc.perform(post("/api/parent/student-invitations")
                        .header("Authorization", "Bearer " + accessToken(parent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentInvitationRequest("controller-student@ringdu.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiverEmail").value("controller-student@ringdu.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("STUDENT는 PARENT에게 연결 초대장을 보낼 수 있다")
    void studentCanCreateParentInvitation() throws Exception {
        User student = saveUser("controller-student-invite@ringdu.com", Role.STUDENT);

        mockMvc.perform(post("/api/student/parent-invitations")
                        .header("Authorization", "Bearer " + accessToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentInvitationRequest("controller-parent@ringdu.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiverEmail").value("controller-parent@ringdu.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("수신자는 받은 연결 초대장을 수락할 수 있다")
    void receiverCanAcceptInvitation() throws Exception {
        User parent = saveUser("controller-accept-parent@ringdu.com", Role.PARENT);
        User student = saveUser("controller-accept-student@ringdu.com", Role.STUDENT);
        Long invitationId = invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest(student.getEmail())
        ).invitationId();

        mockMvc.perform(post("/api/parent-student-invitations/{invitationId}/accept", invitationId)
                        .header("Authorization", "Bearer " + accessToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PARENT는 받은 초대장과 보낸 초대장을 조회할 수 있다")
    void parentCanGetInvitations() throws Exception {
        User parent = saveUser("controller-list-parent@ringdu.com", Role.PARENT);
        invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest("controller-list-student@ringdu.com")
        );

        mockMvc.perform(get("/api/parent/invitations")
                        .header("Authorization", "Bearer " + accessToken(parent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].direction").value("SENT"))
                .andExpect(jsonPath("$.data[0].receiverEmail").value("controller-list-student@ringdu.com"));
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

    private ParentStudentInvitationCreateRequest studentInvitationRequest(String studentEmail) {
        return new ParentStudentInvitationCreateRequest(
                studentEmail,
                "010-2222-3333",
                "자녀 연결 요청입니다."
        );
    }

    private StudentParentInvitationCreateRequest parentInvitationRequest(String parentEmail) {
        return new StudentParentInvitationCreateRequest(
                parentEmail,
                "010-3333-4444",
                "보호자 연결 요청입니다."
        );
    }
}
