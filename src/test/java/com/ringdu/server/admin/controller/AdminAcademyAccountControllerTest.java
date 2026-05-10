package com.ringdu.server.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.admin.dto.CreateAcademyAccountRequest;
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
class AdminAcademyAccountControllerTest {

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
    @DisplayName("ADMIN은 ACADEMY 계정을 생성할 수 있다")
    void adminCanCreateAcademyAccount() throws Exception {
        String token = accessToken(saveUser("admin-create@ringdu.com", Role.ADMIN));

        mockMvc.perform(post("/api/admin/academy-accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("new-academy@ringdu.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("new-academy@ringdu.com"))
                .andExpect(jsonPath("$.data.role").value("ACADEMY"));
    }

    @Test
    @DisplayName("비로그인 사용자는 ACADEMY 계정을 생성할 수 없다")
    void anonymousCannotCreateAcademyAccount() throws Exception {
        mockMvc.perform(post("/api/admin/academy-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("anonymous-academy@ringdu.com"))))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ACADEMY", "TEACHER", "PARENT", "STUDENT"})
    @DisplayName("ADMIN이 아닌 사용자는 ACADEMY 계정을 생성할 수 없다")
    void nonAdminCannotCreateAcademyAccount(Role role) throws Exception {
        String token = accessToken(saveUser(role.name().toLowerCase() + "-create@ringdu.com", role));

        mockMvc.perform(post("/api/admin/academy-accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(role.name().toLowerCase() + "-academy@ringdu.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("중복 이메일로 ACADEMY 계정을 생성할 수 없다")
    void duplicatedEmailCannotCreateAcademyAccount() throws Exception {
        String token = accessToken(saveUser("admin-duplicate@ringdu.com", Role.ADMIN));
        saveUser("duplicated-create@ringdu.com", Role.TEACHER);

        mockMvc.perform(post("/api/admin/academy-accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("duplicated-create@ringdu.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
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

    private CreateAcademyAccountRequest request(String email) {
        return new CreateAcademyAccountRequest(
                email,
                "password1234",
                "링듀수학학원",
                "010-1234-5678"
        );
    }
}
