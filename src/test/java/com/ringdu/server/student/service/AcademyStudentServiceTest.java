package com.ringdu.server.student.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.dto.AcademyStudentCreateRequest;
import com.ringdu.server.student.dto.AcademyStudentResponse;
import com.ringdu.server.student.entity.StudentGuardianAccountLink;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentGuardianAccountLinkRepository;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AcademyStudentServiceTest {

    private AcademyStudentService academyStudentService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StudentGuardianAccountLinkRepository guardianAccountLinkRepository;

    @Mock
    private ParentStudentRelationRepository parentStudentRelationRepository;

    @Mock
    private AcademyRepository academyRepository;

    @Mock
    private UserRepository userRepository;

    private Academy academy;
    private User academyUser;

    @BeforeEach
    void setUp() {
        academy = mock(Academy.class);
        given(academy.getId()).willReturn(10L);
        StudentGuardianLinkService guardianLinkService = new StudentGuardianLinkService(
                guardianAccountLinkRepository,
                parentStudentRelationRepository,
                userRepository
        );
        StudentDetailAssembler detailAssembler = new StudentDetailAssembler(userRepository, guardianLinkService);
        academyStudentService = new AcademyStudentService(
                studentProfileRepository,
                academyRepository,
                detailAssembler,
                guardianLinkService
        );
    }

    @Test
    @DisplayName("학원이 학생을 등록할 수 있다 - 최소 정보(이름만)")
    void createStudent_MinInfo() {
        // given
        AcademyStudentCreateRequest request = new AcademyStudentCreateRequest(
                "김학생", null, null, null, null, null, null, null, null
        );
        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.save(any(StudentProfile.class))).willAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            return profile;
        });

        // when
        AcademyStudentResponse response = academyStudentService.createStudent(1L, request);

        // then
        assertThat(response.name()).isEqualTo("김학생");
        assertThat(response.academyId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(StudentStatus.ACTIVE);
    }

    @Test
    @DisplayName("학원이 학생을 등록할 수 있다 - 전체 정보 및 보호자 연락처 포함")
    void createStudent_FullInfo() {
        // given
        AcademyStudentCreateRequest request = new AcademyStudentCreateRequest(
                "이학생", LocalDate.of(2018, 3, 1), "링듀초", "1학년",
                "student@test.com", "010-1111-2222", "010-3333-4444", null, "특이사항 없음"
        );
        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.save(any(StudentProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AcademyStudentResponse response = academyStudentService.createStudent(1L, request);

        // then
        assertThat(response.name()).isEqualTo("이학생");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(response.guardianPhone()).isEqualTo("010-3333-4444");
    }

    @Test
    @DisplayName("보호자 전화번호로 단일 PARENT 계정 확인 후 학생 등록 시 보호자 계정을 연결한다")
    void createStudent_LinkGuardianParentAccount() {
        AcademyStudentCreateRequest request = new AcademyStudentCreateRequest(
                "최학생", null, "링듀초", "2학년",
                null, null, "010-4444-5555", 100L, "보호자 연결"
        );
        User parent = mockUser(100L, "parent@ringdu.com", "보호자", "010-4444-5555", Role.PARENT);

        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(userRepository.findAllByPhoneAndRoleAndStatus("010-4444-5555", Role.PARENT, UserStatus.ACTIVE))
                .willReturn(List.of(parent));
        given(studentProfileRepository.save(any(StudentProfile.class))).willAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            ReflectionTestUtils.setField(profile, "id", 200L);
            return profile;
        });

        AcademyStudentResponse response = academyStudentService.createStudent(1L, request);

        assertThat(response.guardianAccountLinked()).isTrue();
        assertThat(response.guardianParentUserId()).isEqualTo(100L);
        assertThat(response.guardianParentName()).isEqualTo("보호자");
        assertThat(response.guardianPhone()).isEqualTo("010-4444-5555");
        verify(guardianAccountLinkRepository).save(any(StudentGuardianAccountLink.class));
    }

    @Test
    @DisplayName("보호자 계정이 없어도 학생 등록 가능하고 guardianPhone을 저장한다")
    void createStudent_NoGuardianAccountSavesGuardianPhone() {
        AcademyStudentCreateRequest request = new AcademyStudentCreateRequest(
                "정학생", null, null, null,
                null, null, "010-2222-3333", null, null
        );
        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(userRepository.findAllByPhoneAndRoleAndStatus("010-2222-3333", Role.PARENT, UserStatus.ACTIVE))
                .willReturn(List.of());
        given(studentProfileRepository.save(any(StudentProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        AcademyStudentResponse response = academyStudentService.createStudent(1L, request);

        assertThat(response.guardianPhone()).isEqualTo("010-2222-3333");
        assertThat(response.guardianAccountLinked()).isFalse();
        assertThat(response.guardianParentUserId()).isNull();
    }

    @Test
    @DisplayName("이미 등록된 학생 계정이 있으면 matchedStudentUserExists 가 true 이다")
    void createStudent_MatchedUserExists() {
        // given
        String email = "matched@student.com";
        AcademyStudentCreateRequest request = new AcademyStudentCreateRequest(
                "박학생", null, null, null, email, null, null, null, null
        );
        User matchedUser = mock(User.class);
        given(matchedUser.getRole()).willReturn(Role.STUDENT);

        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(matchedUser));
        given(studentProfileRepository.save(any(StudentProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        AcademyStudentResponse response = academyStudentService.createStudent(1L, request);

        // then
        assertThat(response.matchedStudentUserExists()).isTrue();
        assertThat(response.userId()).isNull();
    }

    @Test
    @DisplayName("다른 학원의 학생 정보는 조회할 수 없다")
    void getStudent_OtherAcademy_ThrowsException() {
        // given
        StudentProfile profile = StudentProfile.builder()
                .academyId(20L) // Other academy
                .name("남의학생")
                .status(StudentStatus.ACTIVE)
                .build();

        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.findById(100L)).willReturn(Optional.of(profile));

        // when & then
        assertThatThrownBy(() -> academyStudentService.getStudent(1L, 100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("보호자 전화번호만 저장된 학생 상세 응답은 guardianPhone을 표시하고 미연결 상태이다")
    void getStudent_GuardianPhoneOnly() {
        StudentProfile profile = StudentProfile.builder()
                .academyId(10L)
                .name("연락처학생")
                .guardianPhone("010-1111-2222")
                .status(StudentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(profile, "id", 250L);

        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.findById(250L)).willReturn(Optional.of(profile));
        given(guardianAccountLinkRepository.findByStudentProfileIdWithParent(250L)).willReturn(Optional.empty());

        AcademyStudentResponse response = academyStudentService.getStudent(1L, 250L);

        assertThat(response.guardianPhone()).isEqualTo("010-1111-2222");
        assertThat(response.guardianAccountLinked()).isFalse();
        assertThat(response.guardianParentUserId()).isNull();
    }

    @Test
    @DisplayName("학생 상세 응답에 보호자 연락처와 계정 연결 상태를 포함한다")
    void getStudent_IncludesGuardianAccount() {
        StudentProfile profile = StudentProfile.builder()
                .academyId(10L)
                .name("상세학생")
                .guardianPhone("010-1111-2222")
                .status(StudentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(profile, "id", 300L);
        User parent = mockUser(301L, "detail-parent@ringdu.com", "상세보호자", "010-9999-8888", Role.PARENT);
        StudentGuardianAccountLink link = mock(StudentGuardianAccountLink.class);

        given(link.getParent()).willReturn(parent);
        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.findById(300L)).willReturn(Optional.of(profile));
        given(guardianAccountLinkRepository.findByStudentProfileIdWithParent(300L)).willReturn(Optional.of(link));

        AcademyStudentResponse response = academyStudentService.getStudent(1L, 300L);

        assertThat(response.guardianAccountLinked()).isTrue();
        assertThat(response.guardianParentUserId()).isEqualTo(301L);
        assertThat(response.guardianParentName()).isEqualTo("상세보호자");
        assertThat(response.guardianPhone()).isEqualTo("010-9999-8888");
    }

    @Test
    @DisplayName("학생 계정에 ACTIVE ParentStudentRelation이 있으면 상세 응답에 보호자 계정을 연결 상태로 포함한다")
    void getStudent_UsesParentStudentRelationWhenStudentUserLinked() {
        StudentProfile profile = StudentProfile.builder()
                .academyId(10L)
                .userId(400L)
                .name("연결학생")
                .status(StudentStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(profile, "id", 401L);
        User parent = mockUser(500L, "relation-parent@ringdu.com", "관계보호자", "010-7777-8888", Role.PARENT);
        ParentStudentRelation relation = mock(ParentStudentRelation.class);

        given(relation.getParent()).willReturn(parent);
        given(academyRepository.findByUserId(1L)).willReturn(Optional.of(academy));
        given(studentProfileRepository.findById(401L)).willReturn(Optional.of(profile));
        given(parentStudentRelationRepository.findAllByStudentIdAndStatusWithParent(
                400L,
                ParentStudentRelationStatus.ACTIVE
        )).willReturn(List.of(relation));

        AcademyStudentResponse response = academyStudentService.getStudent(1L, 401L);

        assertThat(response.guardianAccountLinked()).isTrue();
        assertThat(response.guardianParentUserId()).isEqualTo(500L);
        assertThat(response.guardianParentName()).isEqualTo("관계보호자");
        assertThat(response.guardianPhone()).isEqualTo("010-7777-8888");
        assertThat(response.guardianParentPhone()).isEqualTo("010-7777-8888");
    }

    private User mockUser(Long id, String email, String name, String phone, Role role) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getEmail()).willReturn(email);
        given(user.getName()).willReturn(name);
        given(user.getPhone()).willReturn(phone);
        return user;
    }
}
