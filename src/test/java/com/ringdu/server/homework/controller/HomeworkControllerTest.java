package com.ringdu.server.homework.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.dto.AcademyClassRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassStudentRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomCreateRequest;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.homework.dto.HomeworkCreateRequest;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class HomeworkControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;
    @Autowired AdminAcademySignupApplicationService adminAcademySignupApplicationService;
    @Autowired UserRepository userRepository;
    @Autowired AcademyRepository academyRepository;
    @Autowired AcademyMemberRepository academyMemberRepository;
    @Autowired AcademyClassroomRepository classroomRepository;
    @Autowired AcademyClassRepository classRepository;
    @Autowired StudentProfileRepository studentProfileRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("학생 숙제 이력이 없으면 빈 배열을 반환한다")
    void academyStudentHomeworkHistoryEmpty() throws Exception {
        AcademyContext context = academyContext("homework-empty-academy@ringdu.com");
        StudentProfile student = createStudent(context, "김학생");

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/homeworks", student.getId())
                        .header("Authorization", "Bearer " + context.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("선생님 담당 수업 숙제가 없으면 빈 배열을 반환한다")
    void teacherClassHomeworksEmpty() throws Exception {
        AcademyContext context = academyContext("homework-teacher-empty-academy@ringdu.com");
        User teacher = createTeacher(context);
        Long classId = createClass(context, teacher.getId());

        mockMvc.perform(get("/api/teacher/classes/{classId}/homeworks", classId)
                        .header("Authorization", "Bearer " + accessToken(teacher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("수강 학생이 없는 수업에는 숙제를 배정할 수 없다")
    void cannotCreateHomeworkWithoutStudents() throws Exception {
        AcademyContext context = academyContext("homework-no-students-academy@ringdu.com");
        User teacher = createTeacher(context);
        Long classId = createClass(context, teacher.getId());

        mockMvc.perform(post("/api/teacher/classes/{classId}/homeworks", classId)
                        .header("Authorization", "Bearer " + accessToken(teacher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(homeworkRequest(HomeworkTargetType.CLASS, List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("수강 학생이 없어 숙제를 배정할 수 없습니다."));
    }

    @Test
    @DisplayName("수강 학생이 있으면 숙제를 등록하고 학생 상세 이력에서 조회할 수 있다")
    void canCreateAndQueryHomework() throws Exception {
        AcademyContext context = academyContext("homework-create-academy@ringdu.com");
        User teacher = createTeacher(context);
        Long classId = createClass(context, teacher.getId());
        StudentProfile student = createStudent(context, "이학생");
        addStudent(context, classId, student.getId());

        mockMvc.perform(post("/api/teacher/classes/{classId}/homeworks", classId)
                        .header("Authorization", "Bearer " + accessToken(teacher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(homeworkRequest(HomeworkTargetType.CLASS, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.students.length()").value(1))
                .andExpect(jsonPath("$.data.students[0].status").value("NOT_DONE"));

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/homeworks", student.getId())
                        .header("Authorization", "Bearer " + context.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("단원 문제 풀이"));
    }

    private AcademyContext academyContext(String email) {
        var signup = authService.signupAcademy(new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀학원",
                "홍길동",
                phoneFor(email),
                "06123",
                "서울시 강남구",
                "101호"
        ));
        adminAcademySignupApplicationService.approve(signup.applicationId(), 1L);
        User academyUser = userRepository.findByEmail(email).orElseThrow();
        Academy academy = academyRepository.findByUserId(academyUser.getId()).orElseThrow();
        return new AcademyContext(accessToken(academyUser), academy);
    }

    private User createTeacher(AcademyContext context) {
        User teacher = userRepository.save(User.createLocalUser(
                "teacher-" + context.academy().getId() + "@ringdu.com",
                passwordEncoder.encode("password1234"),
                "테스트 선생",
                "010-3333-%04d".formatted(context.academy().getId()),
                Role.TEACHER
        ));
        academyMemberRepository.save(AcademyMember.createTeacher(context.academy(), teacher));
        return teacher;
    }

    private Long createClass(AcademyContext context, Long teacherUserId) throws Exception {
        mockMvc.perform(post("/api/academies/me/classrooms")
                        .header("Authorization", "Bearer " + context.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassroomCreateRequest("1강의실"))))
                .andExpect(status().isOk());
        Long classroomId = classroomRepository.findAllByAcademyIdAndStatusOrderByDisplayOrderAscIdAsc(
                context.academy().getId(), ScheduleStatus.ACTIVE
        ).get(0).getId();
        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassRequest(
                                "숙제 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                List.of(AcademyClassDayOfWeek.MONDAY),
                                classroomId,
                                teacherUserId,
                                LocalTime.of(16, 0),
                                LocalTime.of(17, 0),
                                null
                        ))))
                .andExpect(status().isOk());
        return classRepository.findSchedule(context.academy().getId(), AcademyClassDayOfWeek.MONDAY, classroomId, ScheduleStatus.ACTIVE)
                .get(0)
                .getId();
    }

    private StudentProfile createStudent(AcademyContext context, String name) {
        return studentProfileRepository.save(StudentProfile.builder()
                .academyId(context.academy().getId())
                .name(name)
                .school("동신중")
                .grade("2")
                .phone("010-0000-0000")
                .guardianPhone("010-1111-1111")
                .status(StudentStatus.ACTIVE)
                .build());
    }

    private void addStudent(AcademyContext context, Long classId, Long studentProfileId) throws Exception {
        mockMvc.perform(post("/api/academies/me/classes/{classId}/students", classId)
                        .header("Authorization", "Bearer " + context.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassStudentRequest(studentProfileId))))
                .andExpect(status().isOk());
    }

    private HomeworkCreateRequest homeworkRequest(HomeworkTargetType targetType, List<Long> studentProfileIds) {
        return new HomeworkCreateRequest(
                "단원 문제 풀이",
                "1단원 문제를 풀어오세요.",
                LocalDate.now().plusDays(1),
                targetType,
                studentProfileIds,
                null
        );
    }

    private String accessToken(User user) {
        return jwtTokenProvider.createAccessToken(user);
    }

    private String phoneFor(String seed) {
        int suffix = Math.abs(seed.hashCode() % 9000) + 1000;
        return "010-5555-" + suffix;
    }

    private record AcademyContext(String academyToken, Academy academy) {
    }
}
