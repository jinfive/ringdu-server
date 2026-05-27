package com.ringdu.server.academy.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AcademyAccountCandidateServiceTest {

    @Autowired
    private AcademyAccountCandidateService candidateService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("email만 일치하는 STUDENT 후보를 검색할 수 있다")
    void searchStudentByEmail() {
        saveUser("candidate1@ringdu.com", "010-1111-1111", Role.STUDENT);
        saveUser("candidate2@ringdu.com", "010-2222-2222", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, "candidate1@ringdu.com", null);

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().get(0).email()).isEqualTo("candidate1@ringdu.com");
    }

    @Test
    @DisplayName("phone만 일치하는 STUDENT 후보를 검색할 수 있다")
    void searchStudentByPhone() {
        saveUser("candidate3@ringdu.com", "010-3333-3333", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, null, "010-3333-3333");

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().get(0).phone()).isEqualTo("010-3333-3333");
    }

    @Test
    @DisplayName("학생 전화번호가 없으면 PARENT phone 후보를 검색할 수 있다")
    void searchParentByPhone() {
        saveUser("parent-candidate@ringdu.com", "010-3333-0000", Role.PARENT);

        var response = candidateService.searchCandidates(Role.PARENT, null, "010-3333-0000");

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().get(0).role()).isEqualTo(Role.PARENT);
        assertThat(response.candidates().get(0).phone()).isEqualTo("010-3333-0000");
    }

    @Test
    @DisplayName("phone으로 TEACHER 후보를 검색할 수 있다")
    void searchTeacherByPhone() {
        saveUser("teacher-candidate@ringdu.com", "010-3333-2222", Role.TEACHER);

        var response = candidateService.searchCandidates(Role.TEACHER, null, "010-3333-2222");

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().get(0).role()).isEqualTo(Role.TEACHER);
        assertThat(response.candidates().get(0).phone()).isEqualTo("010-3333-2222");
    }

    @Test
    @DisplayName("email 없이 account-candidates 호출이 가능하다")
    void searchWithoutEmail() {
        saveUser("phone-only@ringdu.com", "010-3333-1111", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, null, "010-3333-1111");

        assertThat(response.candidates()).hasSize(1);
    }

    @Test
    @DisplayName("복수 후보가 나와도 리스트로 반환한다")
    void searchMultipleCandidates() {
        saveUser("phone-match1@ringdu.com", "010-5555-5555", Role.STUDENT);
        saveUser("phone-match2@ringdu.com", "010-5555-5555", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, null, "010-5555-5555");

        assertThat(response.candidates()).hasSize(2);
        assertThat(response.candidates()).extracting("email")
                .containsExactlyInAnyOrder("phone-match1@ringdu.com", "phone-match2@ringdu.com");
    }

    @Test
    @DisplayName("같은 user가 email과 phone에 모두 매칭되어도 중복 없이 반환한다")
    void searchSameUserMatchedByEmailAndPhoneOnce() {
        saveUser("same@ringdu.com", "010-6666-6666", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, "same@ringdu.com", "010-6666-6666");

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().get(0).email()).isEqualTo("same@ringdu.com");
    }

    @Test
    @DisplayName("role=STUDENT 검색 시 PARENT는 제외한다")
    void searchStudentRoleExcludesParent() {
        saveUser("candidate4@ringdu.com", "010-4444-4444", Role.PARENT);

        var response = candidateService.searchCandidates(Role.STUDENT, "candidate4@ringdu.com", "010-4444-4444");

        assertThat(response.candidates()).isEmpty();
    }

    @Test
    @DisplayName("후보가 없으면 빈 리스트를 반환한다")
    void searchNoCandidatesReturnsEmptyList() {
        saveUser("candidate5@ringdu.com", "010-5555-5555", Role.STUDENT);

        var response = candidateService.searchCandidates(Role.STUDENT, "none@ringdu.com", "010-0000-0000");

        assertThat(response.candidates()).isEmpty();
    }

    private void saveUser(String email, String phone, Role role) {
        userRepository.save(User.createLocalUser(
                email, "password", "이름", phone, role
        ));
    }
}
