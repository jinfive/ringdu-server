package com.ringdu.server.attendance.controller;

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
import com.ringdu.server.attendance.dto.AttendanceRecordSaveRequest;
import com.ringdu.server.attendance.dto.AttendanceSessionCreateRequest;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.attendance.repository.AttendanceSessionRepository;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AttendanceControllerTest {

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
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("담당 TEACHER가 오늘 수업 목록을 조회할 수 있다")
    void teacherCanGetTodayClasses() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-today@ringdu.com");

        mockMvc.perform(get("/api/teacher/today-classes")
                        .header("Authorization", "Bearer " + fixture.teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].classId").value(fixture.classId()))
                .andExpect(jsonPath("$.data[0].className").value("중등 수학 A반"))
                .andExpect(jsonPath("$.data[0].studentCount").value(2));
    }

    @Test
    @DisplayName("담당 TEACHER가 출석부를 생성하면 수강 학생 수만큼 record가 생성된다")
    void teacherCanCreateAttendanceSessionWithRecords() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-create@ringdu.com");

        mockMvc.perform(post("/api/teacher/classes/{classId}/attendance-sessions", fixture.classId())
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceSessionCreateRequest(LocalDate.now()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.classId").value(fixture.classId()))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    @Test
    @DisplayName("같은 날짜 출석부를 중복 생성하면 기존 세션을 반환한다")
    void duplicateAttendanceSessionReturnsExistingSession() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-duplicate@ringdu.com");
        Long firstSessionId = createSession(fixture);

        mockMvc.perform(post("/api/teacher/classes/{classId}/attendance-sessions", fixture.classId())
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceSessionCreateRequest(LocalDate.now()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceSessionId").value(firstSessionId));
    }

    @Test
    @DisplayName("담당이 아닌 TEACHER는 출석부를 생성할 수 없다")
    void unassignedTeacherCannotCreateAttendanceSession() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-forbidden@ringdu.com");
        User otherTeacher = saveUser("attendance-other-teacher@ringdu.com", Role.TEACHER);

        mockMvc.perform(post("/api/teacher/classes/{classId}/attendance-sessions", fixture.classId())
                        .header("Authorization", "Bearer " + accessToken(otherTeacher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceSessionCreateRequest(LocalDate.now()))))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(AttendanceRecordStatus.class)
    @DisplayName("PRESENT/LATE/ABSENT 상태를 저장할 수 있다")
    void teacherCanSaveAttendanceStatuses(AttendanceRecordStatus status) throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-save-" + status.name().toLowerCase() + "@ringdu.com");
        Long sessionId = createSession(fixture);
        AttendanceRecordSaveRequest request = new AttendanceRecordSaveRequest(List.of(
                new AttendanceRecordSaveRequest.RecordItem(fixture.firstStudentId(), status, "메모")
        ));

        mockMvc.perform(put("/api/teacher/attendance-sessions/{sessionId}/records", sessionId)
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.records[0].status").value(status.name()));
    }

    @Test
    @DisplayName("EXCUSED 상태는 요청으로 저장할 수 없다")
    void excusedStatusIsNotAccepted() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-excused@ringdu.com");
        Long sessionId = createSession(fixture);

        mockMvc.perform(put("/api/teacher/attendance-sessions/{sessionId}/records", sessionId)
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "records", List.of(Map.of(
                                        "studentProfileId", fixture.firstStudentId(),
                                        "status", "EXCUSED",
                                        "memo", ""
                                ))
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ACADEMY가 자기 수업의 출석 세션 목록과 상세를 조회할 수 있다")
    void academyCanReadOwnAttendanceSession() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-academy-read@ringdu.com");
        Long sessionId = createSession(fixture);
        saveRecords(fixture, sessionId);

        mockMvc.perform(get("/api/academies/me/classes/{classId}/attendance-sessions", fixture.classId())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].presentCount").value(1))
                .andExpect(jsonPath("$.data[0].lateCount").value(1))
                .andExpect(jsonPath("$.data[0].absentCount").value(0));

        mockMvc.perform(get("/api/academies/me/attendance-sessions/{sessionId}", sessionId)
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    @Test
    @DisplayName("다른 학원 출석 세션은 조회할 수 없다")
    void academyCannotReadOtherAcademyAttendanceSession() throws Exception {
        AttendanceFixture owner = attendanceFixture("attendance-owner@ringdu.com");
        AttendanceFixture other = attendanceFixture("attendance-other@ringdu.com");
        Long otherSessionId = createSession(other);

        mockMvc.perform(get("/api/academies/me/attendance-sessions/{sessionId}", otherSessionId)
                        .header("Authorization", "Bearer " + owner.academyToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ACADEMY가 학생별 출석 기록을 조회할 수 있다")
    void academyCanReadStudentAttendanceRecords() throws Exception {
        AttendanceFixture fixture = attendanceFixture("attendance-student-records@ringdu.com");
        Long sessionId = createSession(fixture);
        saveRecords(fixture, sessionId);

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/attendance-records", fixture.firstStudentId())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].attendanceSessionId").value(sessionId))
                .andExpect(jsonPath("$.data[0].className").value("중등 수학 A반"))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }

    private Long createSession(AttendanceFixture fixture) throws Exception {
        mockMvc.perform(post("/api/teacher/classes/{classId}/attendance-sessions", fixture.classId())
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceSessionCreateRequest(LocalDate.now()))))
                .andExpect(status().isOk());
        return attendanceSessionRepository.findByAcademyClassIdAndAttendanceDate(fixture.classId(), LocalDate.now())
                .orElseThrow()
                .getId();
    }

    private void saveRecords(AttendanceFixture fixture, Long sessionId) throws Exception {
        AttendanceRecordSaveRequest request = new AttendanceRecordSaveRequest(List.of(
                new AttendanceRecordSaveRequest.RecordItem(fixture.firstStudentId(), AttendanceRecordStatus.PRESENT, ""),
                new AttendanceRecordSaveRequest.RecordItem(fixture.secondStudentId(), AttendanceRecordStatus.LATE, "10분 지각")
        ));
        mockMvc.perform(put("/api/teacher/attendance-sessions/{sessionId}/records", sessionId)
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private AttendanceFixture attendanceFixture(String email) throws Exception {
        AcademyContext academyContext = academyContext(email);
        User teacher = createConnectedTeacher(academyContext, "teacher-" + email);
        Long classroomId = createClassroom(academyContext, "1강의실");
        Long classId = createClass(academyContext, classroomId, teacher.getId());
        StudentProfile firstStudent = createStudent(academyContext, "김학생");
        StudentProfile secondStudent = createStudent(academyContext, "이학생");
        addStudent(academyContext, classId, firstStudent.getId());
        addStudent(academyContext, classId, secondStudent.getId());
        return new AttendanceFixture(
                academyContext.token(),
                accessToken(teacher),
                classId,
                firstStudent.getId(),
                secondStudent.getId()
        );
    }

    private AcademyContext academyContext(String email) {
        String token = approvedAcademyAccessToken(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        Academy academy = academyRepository.findByUserId(user.getId()).orElseThrow();
        return new AcademyContext(token, academy);
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
                phoneFor(email),
                role
        ));
    }

    private User createConnectedTeacher(AcademyContext context, String email) {
        User teacher = saveUser(email, Role.TEACHER);
        academyMemberRepository.save(AcademyMember.createTeacher(context.academy(), teacher));
        return teacher;
    }

    private Long createClassroom(AcademyContext context, String name) throws Exception {
        mockMvc.perform(post("/api/academies/me/classrooms")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassroomCreateRequest(name))))
                .andExpect(status().isOk());
        return classroomRepository.findAllByAcademyIdAndStatusOrderByDisplayOrderAscIdAsc(
                context.academy().getId(),
                ScheduleStatus.ACTIVE
        ).stream().filter(room -> room.getName().equals(name)).findFirst().orElseThrow().getId();
    }

    private Long createClass(AcademyContext context, Long classroomId, Long teacherUserId) throws Exception {
        AcademyClassDayOfWeek today = AcademyClassDayOfWeek.valueOf(LocalDate.now().getDayOfWeek().name());
        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassRequest(
                                "중등 수학 A반",
                                today,
                                classroomId,
                                teacherUserId,
                                LocalTime.of(16, 0),
                                LocalTime.of(17, 30),
                                "테스트 메모"
                        ))))
                .andExpect(status().isOk());
        return classRepository.findSchedule(context.academy().getId(), today, classroomId, ScheduleStatus.ACTIVE)
                .stream()
                .filter(academyClass -> academyClass.getTeacherUserId().equals(teacherUserId))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private void addStudent(AcademyContext context, Long classId, Long studentProfileId) throws Exception {
        mockMvc.perform(post("/api/academies/me/classes/{classId}/students", classId)
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassStudentRequest(studentProfileId))))
                .andExpect(status().isOk());
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

    private AcademySignupRequest academySignupRequest(String email) {
        return new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀수학학원",
                "홍길동",
                phoneFor(email),
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        );
    }

    private String phoneFor(String email) {
        int suffix = Math.abs(email.hashCode() % 10_000);
        return "010-88%02d-%04d".formatted(suffix / 100, suffix % 10000);
    }

    private record AcademyContext(String token, Academy academy) {
    }

    private record AttendanceFixture(
            String academyToken,
            String teacherToken,
            Long classId,
            Long firstStudentId,
            Long secondStudentId
    ) {
    }
}
