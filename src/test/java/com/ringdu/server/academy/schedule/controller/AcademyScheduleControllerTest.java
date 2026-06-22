package com.ringdu.server.academy.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.dto.AcademyClassRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassStudentRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomCreateRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomUpdateRequest;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
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

import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcademyScheduleControllerTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ACADEMY가 강의실을 생성하고 자기 강의실 목록을 조회할 수 있다")
    void academyCanCreateAndListClassrooms() throws Exception {
        String token = approvedAcademyAccessToken("schedule-classroom@ringdu.com");

        mockMvc.perform(post("/api/academies/me/classrooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassroomCreateRequest("1강의실"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("1강의실"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.displayOrder").value(1));

        mockMvc.perform(get("/api/academies/me/classrooms")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("1강의실"));
    }

    @Test
    @DisplayName("다른 학원의 강의실은 수정/삭제할 수 없다")
    void cannotUpdateOrDeleteOtherAcademyClassroom() throws Exception {
        AcademyContext owner = academyContext("schedule-room-owner@ringdu.com");
        AcademyContext other = academyContext("schedule-room-other@ringdu.com");
        Long classroomId = createClassroom(owner, "1강의실");

        mockMvc.perform(put("/api/academies/me/classrooms/{classroomId}", classroomId)
                        .header("Authorization", "Bearer " + other.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassroomUpdateRequest("수정강의실"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/academies/me/classrooms/{classroomId}", classroomId)
                        .header("Authorization", "Bearer " + other.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ACADEMY가 수업을 생성할 수 있다")
    void academyCanCreateClass() throws Exception {
        AcademyContext context = academyContext("schedule-create-class@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long teacherUserId = createConnectedTeacher(context, "schedule-teacher@ringdu.com");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "중등 수학 A반",
                                AcademyClassDayOfWeek.MONDAY,
                                classroomId,
                                teacherUserId,
                                "16:00",
                                "17:30"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("중등 수학 A반"))
                .andExpect(jsonPath("$.data.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.data.dayLabel").value("월"))
                .andExpect(jsonPath("$.data.classroomName").value("1강의실"))
                .andExpect(jsonPath("$.data.teacherName").value("테스트 선생"))
                .andExpect(jsonPath("$.data.startTime").value("16:00"))
                .andExpect(jsonPath("$.data.endTime").value("17:30"));
    }

    @Test
    @DisplayName("ACADEMY는 여러 요일 수업을 생성하고 요일별 조회에서 모두 확인할 수 있다")
    void academyCanCreateMultiDayClass() throws Exception {
        AcademyContext context = academyContext("schedule-create-multi-day@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassRequest(
                                "화목 수업",
                                null,
                                List.of(AcademyClassDayOfWeek.TUESDAY, AcademyClassDayOfWeek.THURSDAY),
                                classroomId,
                                null,
                                LocalTime.parse("16:30"),
                                LocalTime.parse("18:00"),
                                "테스트 메모"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeeks[0]").value("TUESDAY"))
                .andExpect(jsonPath("$.data.dayOfWeeks[1]").value("THURSDAY"));

        mockMvc.perform(get("/api/academies/me/classes")
                        .param("dayOfWeek", "THURSDAY")
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("화목 수업"));
    }

    @Test
    @DisplayName("ACADEMY는 수업 요일과 시간을 수정할 수 있다")
    void academyCanUpdateClassTimeAndDays() throws Exception {
        AcademyContext context = academyContext("schedule-update-class@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long classId = createClass(context, "수정 전 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");

        mockMvc.perform(put("/api/academies/me/classes/{classId}", classId)
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassRequest(
                                "수정 후 수업",
                                null,
                                List.of(AcademyClassDayOfWeek.MONDAY, AcademyClassDayOfWeek.WEDNESDAY, AcademyClassDayOfWeek.FRIDAY),
                                classroomId,
                                null,
                                LocalTime.parse("16:30"),
                                LocalTime.parse("18:00"),
                                "수정 메모"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 후 수업"))
                .andExpect(jsonPath("$.data.startTime").value("16:30"))
                .andExpect(jsonPath("$.data.endTime").value("18:00"))
                .andExpect(jsonPath("$.data.dayOfWeeks.length()").value(3));
    }

    @Test
    @DisplayName("수업 수정 시 같은 요일/강의실/시간이 겹치면 실패한다")
    void overlappingUpdateFails() throws Exception {
        AcademyContext context = academyContext("schedule-update-overlap@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        createClass(context, "기존 수업", AcademyClassDayOfWeek.TUESDAY, classroomId, "16:00", "17:30");
        Long classId = createClass(context, "수정 대상", AcademyClassDayOfWeek.THURSDAY, classroomId, "18:00", "19:00");

        mockMvc.perform(put("/api/academies/me/classes/{classId}", classId)
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassRequest(
                                "겹치는 수정",
                                null,
                                List.of(AcademyClassDayOfWeek.TUESDAY, AcademyClassDayOfWeek.THURSDAY),
                                classroomId,
                                null,
                                LocalTime.parse("17:00"),
                                LocalTime.parse("18:00"),
                                "테스트 메모"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("startTime이 endTime보다 빠르지 않으면 수업 생성에 실패한다")
    void invalidTimeFails() throws Exception {
        AcademyContext context = academyContext("schedule-invalid-time@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "잘못된 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                classroomId,
                                null,
                                "18:00",
                                "18:00"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 요일/강의실/시간이 겹치면 수업 생성에 실패한다")
    void overlappingSameDayRoomFails() throws Exception {
        AcademyContext context = academyContext("schedule-overlap@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        createClass(context, "기존 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "겹치는 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                classroomId,
                                null,
                                "17:00",
                                "18:00"
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("다른 요일이면 같은 강의실 같은 시간 수업 생성이 가능하다")
    void sameRoomTimeDifferentDayAllowed() throws Exception {
        AcademyContext context = academyContext("schedule-other-day@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        createClass(context, "월요일 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "화요일 수업",
                                AcademyClassDayOfWeek.TUESDAY,
                                classroomId,
                                null,
                                "16:00",
                                "17:30"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("다른 강의실이면 같은 요일 같은 시간 수업 생성이 가능하다")
    void sameDayTimeDifferentRoomAllowed() throws Exception {
        AcademyContext context = academyContext("schedule-other-room@ringdu.com");
        Long classroomOneId = createClassroom(context, "1강의실");
        Long classroomTwoId = createClassroom(context, "2강의실");
        createClass(context, "1강의실 수업", AcademyClassDayOfWeek.MONDAY, classroomOneId, "16:00", "17:30");

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "2강의실 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                classroomTwoId,
                                null,
                                "16:00",
                                "17:30"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("학원에 연결되지 않은 teacherUserId는 지정할 수 없다")
    void unconnectedTeacherFails() throws Exception {
        AcademyContext context = academyContext("schedule-unconnected-teacher@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        User teacher = saveUser("not-connected-teacher@ringdu.com", Role.TEACHER);

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "담당자 오류 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                classroomId,
                                teacher.getId(),
                                "16:00",
                                "17:30"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수업 목록 조회에서 dayOfWeek와 classroomId 필터가 동작한다")
    void classListFiltersWork() throws Exception {
        AcademyContext context = academyContext("schedule-filter@ringdu.com");
        Long classroomOneId = createClassroom(context, "1강의실");
        Long classroomTwoId = createClassroom(context, "2강의실");
        createClass(context, "월 1강의실", AcademyClassDayOfWeek.MONDAY, classroomOneId, "16:00", "17:30");
        createClass(context, "화 1강의실", AcademyClassDayOfWeek.TUESDAY, classroomOneId, "16:00", "17:30");
        createClass(context, "월 2강의실", AcademyClassDayOfWeek.MONDAY, classroomTwoId, "18:00", "19:00");

        mockMvc.perform(get("/api/academies/me/classes")
                        .param("dayOfWeek", "MONDAY")
                        .param("classroomId", classroomOneId.toString())
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("월 1강의실"));
    }

    @Test
    @DisplayName("수업 상세 조회 시 수강 학생 목록을 포함한다")
    void classDetailIncludesStudents() throws Exception {
        AcademyContext context = academyContext("schedule-detail-students@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long classId = createClass(context, "상세 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");
        StudentProfile student = createStudent(context, "김학생");
        addStudent(context, classId, student.getId());

        mockMvc.perform(get("/api/academies/me/classes/{classId}", classId)
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentCount").value(1))
                .andExpect(jsonPath("$.data.students[0].studentProfileId").value(student.getId()))
                .andExpect(jsonPath("$.data.students[0].name").value("김학생"))
                .andExpect(jsonPath("$.data.students[0].phone").value("010-0000-0000"))
                .andExpect(jsonPath("$.data.students[0].guardianPhone").value("010-1111-1111"));
    }

    @Test
    @DisplayName("ACADEMY가 자기 학원 학생을 이름으로 검색할 수 있다")
    void canSearchOwnAcademyStudentsByName() throws Exception {
        AcademyContext context = academyContext("schedule-search-students@ringdu.com");
        StudentProfile student = createStudent(context, "김학생");
        createStudent(context, "이학생");

        mockMvc.perform(get("/api/academies/me/students/search")
                        .param("keyword", "김")
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(student.getId()))
                .andExpect(jsonPath("$.data[0].name").value("김학생"))
                .andExpect(jsonPath("$.data[0].school").value("동신중"))
                .andExpect(jsonPath("$.data[0].grade").value("2"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("학생 이름 검색 결과에 다른 학원 학생은 포함되지 않는다")
    void searchStudentsExcludesOtherAcademyStudents() throws Exception {
        AcademyContext owner = academyContext("schedule-search-owner@ringdu.com");
        AcademyContext other = academyContext("schedule-search-other@ringdu.com");
        createStudent(owner, "김학생");
        createStudent(other, "김학생");

        mockMvc.perform(get("/api/academies/me/students/search")
                        .param("keyword", "김")
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("학생 이름 검색어가 비어 있으면 빈 배열을 반환한다")
    void searchStudentsBlankKeywordReturnsEmptyList() throws Exception {
        AcademyContext context = academyContext("schedule-search-blank@ringdu.com");
        createStudent(context, "김학생");

        mockMvc.perform(get("/api/academies/me/students/search")
                        .param("keyword", " ")
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("수강 학생을 추가할 수 있다")
    void canAddClassStudent() throws Exception {
        AcademyContext context = academyContext("schedule-add-student@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long classId = createClass(context, "수강 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");
        StudentProfile student = createStudent(context, "이학생");

        mockMvc.perform(post("/api/academies/me/classes/{classId}/students", classId)
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassStudentRequest(student.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentCount").value(1))
                .andExpect(jsonPath("$.data.students[0].name").value("이학생"));
    }

    @Test
    @DisplayName("이미 수강 중인 학생은 중복 추가할 수 없다")
    void cannotAddDuplicateClassStudent() throws Exception {
        AcademyContext context = academyContext("schedule-add-duplicate-student@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long classId = createClass(context, "수강 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");
        StudentProfile student = createStudent(context, "이학생");
        addStudent(context, classId, student.getId());

        mockMvc.perform(post("/api/academies/me/classes/{classId}/students", classId)
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassStudentRequest(student.getId()))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("학생별 수강 중인 수업 목록을 조회할 수 있다")
    void canGetStudentClasses() throws Exception {
        AcademyContext context = academyContext("schedule-student-classes@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long firstClassId = createClass(context, "수학", AcademyClassDayOfWeek.TUESDAY, classroomId, "16:00", "17:30");
        createClass(context, "영어", AcademyClassDayOfWeek.WEDNESDAY, classroomId, "18:00", "19:00");
        StudentProfile student = createStudent(context, "김학생");
        addStudent(context, firstClassId, student.getId());

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/classes", student.getId())
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].classId").value(firstClassId))
                .andExpect(jsonPath("$.data[0].name").value("수학"))
                .andExpect(jsonPath("$.data[0].dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.data[0].dayLabel").value("화"))
                .andExpect(jsonPath("$.data[0].classroomName").value("1강의실"))
                .andExpect(jsonPath("$.data[0].startTime").value("16:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("17:30"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("다른 학원 학생의 수강 수업 목록은 조회할 수 없다")
    void cannotGetOtherAcademyStudentClasses() throws Exception {
        AcademyContext owner = academyContext("schedule-student-classes-owner@ringdu.com");
        AcademyContext other = academyContext("schedule-student-classes-other@ringdu.com");
        StudentProfile otherStudent = createStudent(other, "다른학생");

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/classes", otherStudent.getId())
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("수업에서 제외된 학생의 수강 수업은 조회되지 않는다")
    void removedClassStudentIsNotListed() throws Exception {
        AcademyContext context = academyContext("schedule-student-classes-removed@ringdu.com");
        Long classroomId = createClassroom(context, "1강의실");
        Long classId = createClass(context, "제외 수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");
        StudentProfile student = createStudent(context, "박학생");
        addStudent(context, classId, student.getId());

        mockMvc.perform(delete("/api/academies/me/classes/{classId}/students/{studentProfileId}", classId, student.getId())
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/classes", student.getId())
                        .header("Authorization", "Bearer " + context.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("다른 학원 학생은 수업에 추가할 수 없다")
    void cannotAddOtherAcademyStudent() throws Exception {
        AcademyContext owner = academyContext("schedule-student-owner@ringdu.com");
        AcademyContext other = academyContext("schedule-student-other@ringdu.com");
        Long classroomId = createClassroom(owner, "1강의실");
        Long classId = createClass(owner, "수업", AcademyClassDayOfWeek.MONDAY, classroomId, "16:00", "17:30");
        StudentProfile otherStudent = createStudent(other, "다른학생");

        mockMvc.perform(post("/api/academies/me/classes/{classId}/students", classId)
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassStudentRequest(otherStudent.getId()))))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"ADMIN", "TEACHER", "PARENT", "STUDENT"})
    @DisplayName("ACADEMY 외 role은 수업을 생성할 수 없다")
    void nonAcademyCannotCreateClass(Role role) throws Exception {
        String token = accessToken(saveUser("schedule-non-academy-" + role.name().toLowerCase() + "@ringdu.com", role));

        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(
                                "권한 없는 수업",
                                AcademyClassDayOfWeek.MONDAY,
                                1L,
                                null,
                                "16:00",
                                "17:30"
                        ))))
                .andExpect(status().isForbidden());
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
                "010-1234-5678",
                role
        ));
    }

    private Long createConnectedTeacher(AcademyContext context, String email) {
        User teacher = userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 선생",
                "010-1111-2222",
                Role.TEACHER
        ));
        academyMemberRepository.save(AcademyMember.createTeacher(context.academy(), teacher));
        return teacher.getId();
    }

    private Long createClassroom(AcademyContext context, String name) throws Exception {
        mockMvc.perform(post("/api/academies/me/classrooms")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AcademyClassroomCreateRequest(name))))
                .andExpect(status().isOk());
        return classroomRepository.findAllByAcademyIdAndStatusOrderByDisplayOrderAscIdAsc(
                context.academy().getId(),
                com.ringdu.server.academy.schedule.entity.ScheduleStatus.ACTIVE
        ).stream().filter(room -> room.getName().equals(name)).findFirst().orElseThrow().getId();
    }

    private Long createClass(
            AcademyContext context,
            String name,
            AcademyClassDayOfWeek dayOfWeek,
            Long classroomId,
            String startTime,
            String endTime
    ) throws Exception {
        mockMvc.perform(post("/api/academies/me/classes")
                        .header("Authorization", "Bearer " + context.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classRequest(name, dayOfWeek, classroomId, null, startTime, endTime))))
                .andExpect(status().isOk());
        return classRepository.findSchedule(
                context.academy().getId(),
                dayOfWeek,
                classroomId,
                com.ringdu.server.academy.schedule.entity.ScheduleStatus.ACTIVE
        ).stream().filter(academyClass -> academyClass.getName().equals(name)).findFirst().orElseThrow().getId();
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

    private AcademyClassRequest classRequest(
            String name,
            AcademyClassDayOfWeek dayOfWeek,
            Long classroomId,
            Long teacherUserId,
            String startTime,
            String endTime
    ) {
        return new AcademyClassRequest(
                name,
                dayOfWeek,
                java.util.List.of(dayOfWeek),
                classroomId,
                teacherUserId,
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                "테스트 메모"
        );
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
        return "010-99%02d-%04d".formatted(suffix / 100, suffix % 10000);
    }

    private record AcademyContext(String token, Academy academy) {
    }
}
