package com.ringdu.server.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStatus;
import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.homework.repository.HomeworkRepository;
import com.ringdu.server.homework.repository.HomeworkStudentRepository;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HomeworkControllerTest {

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
    private AcademyRepository academyRepository;

    @Autowired
    private AcademyMemberRepository academyMemberRepository;

    @Autowired
    private AcademyClassroomRepository classroomRepository;

    @Autowired
    private AcademyClassRepository classRepository;

    @Autowired
    private AcademyClassStudentRepository classStudentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ParentStudentRelationRepository parentStudentRelationRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private HomeworkStudentRepository homeworkStudentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("담당 TEACHER가 수업 전체 숙제를 생성하면 ACTIVE 학생 모두 NOT_DONE으로 배정된다")
    void teacherCreatesClassHomeworkForAllActiveStudents() throws Exception {
        HomeworkFixture fixture = fixture("homework-class@ringdu.com");
        StudentProfile inactiveClassStudent = saveStudent(fixture.academy(), "비활성수강생", null);
        AcademyClassStudent inactiveLink = AcademyClassStudent.create(fixture.classId(), inactiveClassStudent.getId());
        inactiveLink.deactivate();
        classStudentRepository.save(inactiveLink);

        createHomework(fixture, "CLASS", List.of());

        Homework homework = latestHomework(fixture.classId());
        List<HomeworkStudent> assignments = homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homework.getId());
        org.assertj.core.api.Assertions.assertThat(assignments).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(assignments)
                .allMatch(item -> item.getStatus() == HomeworkStudentStatus.NOT_DONE);
    }

    @Test
    @DisplayName("TEACHER는 담당 ACTIVE 수업과 ACTIVE 수강 학생 목록을 조회할 수 있다")
    void teacherGetsAssignedClassesAndStudents() throws Exception {
        HomeworkFixture fixture = fixture("homework-class-list@ringdu.com");

        mockMvc.perform(get("/api/teacher/classes")
                        .header("Authorization", bearer(fixture.teacher())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].classId").value(fixture.classId()))
                .andExpect(jsonPath("$.data[0].studentCount").value(2))
                .andExpect(jsonPath("$.data[0].students.length()").value(2));
    }

    @Test
    @DisplayName("담당 TEACHER가 특정 학생에게 개별 숙제를 생성할 수 있다")
    void teacherCreatesIndividualHomework() throws Exception {
        HomeworkFixture fixture = fixture("homework-individual@ringdu.com");

        createHomework(fixture, "INDIVIDUAL", List.of(fixture.firstStudentId()));

        Homework homework = latestHomework(fixture.classId());
        mockMvc.perform(get("/api/teacher/classes/{classId}/homeworks/{homeworkId}", fixture.classId(), homework.getId())
                        .header("Authorization", bearer(fixture.teacher())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.students.length()").value(1))
                .andExpect(jsonPath("$.data.students[0].studentProfileId").value(fixture.firstStudentId()))
                .andExpect(jsonPath("$.data.students[0].status").value("NOT_DONE"));
    }

    @Test
    @DisplayName("담당 수업 학생이 아닌 학생에게 개별 숙제를 배정할 수 없다")
    void teacherCannotAssignStudentOutsideClass() throws Exception {
        HomeworkFixture fixture = fixture("homework-outside-student@ringdu.com");
        StudentProfile outsideStudent = saveStudent(fixture.academy(), "수업외학생", null);

        mockMvc.perform(post("/api/teacher/classes/{classId}/homeworks", fixture.classId())
                        .header("Authorization", bearer(fixture.teacher()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(homeworkRequest("INDIVIDUAL", List.of(outsideStudent.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("담당하지 않는 TEACHER는 숙제를 생성할 수 없다")
    void unassignedTeacherCannotCreateHomework() throws Exception {
        HomeworkFixture fixture = fixture("homework-other-teacher@ringdu.com");
        User otherTeacher = saveUser("other-homework-teacher@ringdu.com", Role.TEACHER);

        mockMvc.perform(post("/api/teacher/classes/{classId}/homeworks", fixture.classId())
                        .header("Authorization", bearer(otherTeacher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(homeworkRequest("CLASS", List.of())))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(HomeworkStudentStatus.class)
    @DisplayName("담당 TEACHER는 학생별 상태를 DONE 또는 NOT_DONE으로 변경할 수 있다")
    void teacherUpdatesHomeworkStudentStatus(HomeworkStudentStatus statusValue) throws Exception {
        HomeworkFixture fixture = fixture("homework-status-" + statusValue.name().toLowerCase() + "@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        Homework homework = latestHomework(fixture.classId());
        HomeworkStudent assignment = homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homework.getId()).get(0);

        mockMvc.perform(patch("/api/teacher/homework-students/{homeworkStudentId}", assignment.getId())
                        .header("Authorization", bearer(fixture.teacher()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", statusValue.name(),
                                "memo", "검사 메모"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.students[0].status").value(statusValue.name()));
    }

    @Test
    @DisplayName("DONE과 NOT_DONE 외 숙제 상태 요청은 거부된다")
    void invalidHomeworkStudentStatusIsRejected() throws Exception {
        HomeworkFixture fixture = fixture("homework-invalid-status@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        Homework homework = latestHomework(fixture.classId());
        HomeworkStudent assignment = homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homework.getId()).get(0);

        mockMvc.perform(patch("/api/teacher/homework-students/{homeworkStudentId}", assignment.getId())
                        .header("Authorization", bearer(fixture.teacher()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CHECKED","memo":"잘못된 상태"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("STUDENT는 본인 프로필에 배정된 숙제만 조회한다")
    void studentGetsOwnHomeworks() throws Exception {
        HomeworkFixture fixture = fixture("homework-student-view@ringdu.com");
        createHomework(fixture, "INDIVIDUAL", List.of(fixture.firstStudentId()));

        mockMvc.perform(get("/api/student/homeworks")
                        .header("Authorization", bearer(fixture.firstStudentUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.firstStudentId()))
                .andExpect(jsonPath("$.data[0].statusLabel").value("안해옴"));

        mockMvc.perform(get("/api/student/homeworks")
                        .header("Authorization", bearer(fixture.secondStudentUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("PARENT는 ACTIVE 관계로 연결된 자녀 숙제를 조회할 수 있다")
    void parentGetsConnectedChildHomeworks() throws Exception {
        HomeworkFixture fixture = fixture("homework-parent-view@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        User parent = saveUser("homework-parent@ringdu.com", Role.PARENT);
        parentStudentRelationRepository.save(ParentStudentRelation.create(parent, fixture.firstStudentUser()));

        mockMvc.perform(get("/api/parent/children/{studentProfileId}/homeworks", fixture.firstStudentId())
                        .header("Authorization", bearer(parent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentName").value("김학생"));
    }

    @Test
    @DisplayName("연결되지 않은 PARENT는 학생 숙제를 조회할 수 없다")
    void parentCannotGetUnconnectedChildHomeworks() throws Exception {
        HomeworkFixture fixture = fixture("homework-parent-forbidden@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        User parent = saveUser("homework-unconnected-parent@ringdu.com", Role.PARENT);

        mockMvc.perform(get("/api/parent/children/{studentProfileId}/homeworks", fixture.firstStudentId())
                        .header("Authorization", bearer(parent)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ACADEMY는 자기 학원 학생 숙제 이력을 조회할 수 있다")
    void academyGetsOwnStudentHomeworks() throws Exception {
        HomeworkFixture fixture = fixture("homework-academy-view@ringdu.com");
        createHomework(fixture, "CLASS", List.of());

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/homeworks", fixture.firstStudentId())
                        .header("Authorization", bearer(fixture.academy().getUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("ACADEMY는 다른 학원 학생 숙제 이력을 조회할 수 없다")
    void academyCannotGetOtherAcademyStudentHomeworks() throws Exception {
        HomeworkFixture fixture = fixture("homework-academy-forbidden@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        Academy otherAcademy = createAcademy("other-homework-academy@ringdu.com");

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/homeworks", fixture.firstStudentId())
                        .header("Authorization", bearer(otherAcademy.getUser())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("숙제 삭제는 DELETED 상태로 변경되고 조회에서 제외된다")
    void teacherSoftDeletesHomework() throws Exception {
        HomeworkFixture fixture = fixture("homework-delete@ringdu.com");
        createHomework(fixture, "CLASS", List.of());
        Homework homework = latestHomework(fixture.classId());

        mockMvc.perform(delete("/api/teacher/classes/{classId}/homeworks/{homeworkId}", fixture.classId(), homework.getId())
                        .header("Authorization", bearer(fixture.teacher())))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(homeworkRepository.findById(homework.getId()).orElseThrow().getStatus())
                .isEqualTo(HomeworkStatus.DELETED);
        mockMvc.perform(get("/api/teacher/classes/{classId}/homeworks", fixture.classId())
                        .header("Authorization", bearer(fixture.teacher())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private void createHomework(HomeworkFixture fixture, String targetType, List<Long> studentIds) throws Exception {
        mockMvc.perform(post("/api/teacher/classes/{classId}/homeworks", fixture.classId())
                        .header("Authorization", bearer(fixture.teacher()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(homeworkRequest(targetType, studentIds)))
                .andExpect(status().isOk());
    }

    private String homeworkRequest(String targetType, List<Long> studentIds) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", "개념원리 p.30~35",
                "content", "다음 수업 전까지 풀어오기",
                "dueDate", LocalDate.now().plusDays(3).toString(),
                "targetType", targetType,
                "studentProfileIds", studentIds,
                "memo", "기본문제 중심"
        ));
    }

    private Homework latestHomework(Long classId) {
        return homeworkRepository.findAllByClassIdAndStatusOrderByDueDateDescIdDesc(classId, HomeworkStatus.ACTIVE)
                .get(0);
    }

    private HomeworkFixture fixture(String academyEmail) {
        Academy academy = createAcademy(academyEmail);
        User teacher = saveUser("teacher-" + academyEmail, Role.TEACHER);
        academyMemberRepository.save(AcademyMember.createTeacher(academy, teacher));
        AcademyClassroom classroom = classroomRepository.save(AcademyClassroom.create(academy.getId(), "1강의실", 1));
        AcademyClass academyClass = classRepository.save(AcademyClass.create(
                academy.getId(),
                classroom.getId(),
                teacher.getId(),
                "중등수학 A반",
                AcademyClassDayOfWeek.MONDAY,
                LocalTime.of(16, 0),
                LocalTime.of(17, 30),
                null
        ));
        User firstStudentUser = saveUser("student-first-" + academyEmail, Role.STUDENT);
        User secondStudentUser = saveUser("student-second-" + academyEmail, Role.STUDENT);
        StudentProfile firstStudent = saveStudent(academy, "김학생", firstStudentUser.getId());
        StudentProfile secondStudent = saveStudent(academy, "이학생", secondStudentUser.getId());
        classStudentRepository.save(AcademyClassStudent.create(academyClass.getId(), firstStudent.getId()));
        classStudentRepository.save(AcademyClassStudent.create(academyClass.getId(), secondStudent.getId()));
        return new HomeworkFixture(
                academy,
                teacher,
                academyClass.getId(),
                firstStudent.getId(),
                secondStudent.getId(),
                firstStudentUser,
                secondStudentUser
        );
    }

    private Academy createAcademy(String email) {
        var signup = authService.signupAcademy(new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀수학학원",
                "홍길동",
                phoneFor(email),
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        ));
        adminAcademySignupApplicationService.approve(signup.applicationId(), 1L);
        return academyRepository.findByUserId(userRepository.findByEmail(email).orElseThrow().getId()).orElseThrow();
    }

    private StudentProfile saveStudent(Academy academy, String name, Long userId) {
        return studentProfileRepository.save(StudentProfile.builder()
                .academyId(academy.getId())
                .userId(userId)
                .name(name)
                .school("동신중")
                .grade("2")
                .status(StudentStatus.ACTIVE)
                .build());
    }

    private User saveUser(String email, Role role) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 사용자",
                phoneFor(email),
                role
        ));
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenProvider.createAccessToken(user);
    }

    private String phoneFor(String value) {
        int suffix = Math.abs(value.hashCode() % 10_000);
        return "010-77%02d-%04d".formatted(suffix / 100, suffix % 10_000);
    }

    private record HomeworkFixture(
            Academy academy,
            User teacher,
            Long classId,
            Long firstStudentId,
            Long secondStudentId,
            User firstStudentUser,
            User secondStudentUser
    ) {
    }
}
