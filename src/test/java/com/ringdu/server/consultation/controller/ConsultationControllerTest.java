package com.ringdu.server.consultation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.consultation.dto.ConsultationAvailabilityRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestCreateRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationTopic;
import com.ringdu.server.consultation.entity.ConsultationType;
import com.ringdu.server.consultation.repository.ConsultationAvailabilityRepository;
import com.ringdu.server.consultation.repository.ConsultationMemoRepository;
import com.ringdu.server.consultation.repository.ConsultationRequestRepository;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
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
class ConsultationControllerTest {

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
    private AcademyClassRepository classRepository;

    @Autowired
    private AcademyClassStudentRepository classStudentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ParentStudentRelationRepository parentStudentRelationRepository;

    @Autowired
    private ConsultationAvailabilityRepository consultationAvailabilityRepository;

    @Autowired
    private ConsultationRequestRepository consultationRequestRepository;

    @Autowired
    private ConsultationMemoRepository consultationMemoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ACADEMY가 상담 가능 시간을 생성할 수 있다")
    void academyCanCreateAvailability() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-create@ringdu.com");

        mockMvc.perform(post("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest(DayOfWeek.MONDAY, 14, 15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("상담 가능 시간의 시작 시간이 종료 시간보다 늦으면 실패한다")
    void availabilityStartTimeMustBeBeforeEndTime() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-invalid-time@ringdu.com");

        mockMvc.perform(post("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConsultationAvailabilityRequest(
                                DayOfWeek.MONDAY,
                                LocalTime.of(15, 0),
                                LocalTime.of(14, 0),
                                ConsultationType.ALL
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 요일에 ACTIVE 상담 가능 시간이 겹치면 실패한다")
    void availabilityCannotOverlap() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-overlap@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);

        mockMvc.perform(post("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest(DayOfWeek.MONDAY, 15, 17))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ACADEMY가 상담 가능 시간 목록을 조회하고 비활성화할 수 있다")
    void academyCanListAndDeactivateAvailability() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-list-delete@ringdu.com");
        Long availabilityId = createAvailability(fixture, DayOfWeek.MONDAY, 14, 15);

        mockMvc.perform(get("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(delete("/api/academies/me/consultation-availability/{availabilityId}", availabilityId)
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("ACADEMY가 상담 가능 시간을 수정할 수 있다")
    void academyCanUpdateAvailability() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-update@ringdu.com");
        Long availabilityId = createAvailability(fixture, DayOfWeek.MONDAY, 14, 15);

        mockMvc.perform(put("/api/academies/me/consultation-availability/{availabilityId}", availabilityId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest(DayOfWeek.TUESDAY, 16, 17))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeek").value("TUESDAY"))
                .andExpect(jsonPath("$.data.startTime").value("16:00:00"));
    }

    @Test
    @DisplayName("PARENT가 연결된 자녀 상담 요청을 생성할 수 있다")
    void parentCanCreateConsultationRequestForConnectedChild() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-parent-create@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreate(fixture, monday(), 14, 15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentProfileId").value(fixture.studentProfileId()))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                .andExpect(jsonPath("$.data.topicLabel").value("학습 상담"));
    }

    @Test
    @DisplayName("프론트 상담 플로우의 주요 API와 HH:mm 시간 포맷이 동작한다")
    void frontendConsultationFlowWorksWithHourMinuteTimeFormat() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-frontend-flow@ringdu.com");

        mockMvc.perform(post("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "dayOfWeek", "MONDAY",
                                "startTime", "14:00",
                                "endTime", "16:00",
                                "consultationType", "ALL"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startTime").value("14:00:00"));

        mockMvc.perform(get("/api/parent/consultation-options")
                        .header("Authorization", "Bearer " + fixture.parentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.studentProfileId()))
                .andExpect(jsonPath("$.data[0].teachers.length()").value(1));

        mockMvc.perform(get("/api/academies/{academyId}/consultation-availability", fixture.academy().getId())
                        .queryParam("type", "ENROLLED_STUDENT")
                        .header("Authorization", "Bearer " + fixture.parentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "academyId", fixture.academy().getId(),
                                "studentProfileId", fixture.studentProfileId(),
                                "teacherUserId", fixture.teacher().getId(),
                                "requestedDate", monday().toString(),
                                "requestedStartTime", "14:00",
                                "requestedEndTime", "15:00",
                                "topic", "STUDY",
                                "content", "수학 학습 상태 상담을 요청합니다."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestedStartTime").value("14:00:00"))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        Long requestId = consultationRequestRepository.findAllByParentUserIdOrderByCreatedAtDescIdDesc(fixture.parent().getId())
                .stream()
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .queryParam("from", monday().toString())
                        .queryParam("to", monday().toString())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].consultationRequestId").value(requestId));

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/approve", requestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "확인했습니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/complete", requestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "완료했습니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PARENT는 연결되지 않은 자녀 상담 요청을 생성할 수 없다")
    void parentCannotCreateConsultationRequestForUnconnectedChild() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-unconnected@ringdu.com");
        ConsultationFixture other = consultationFixture("consult-unconnected-other@ringdu.com");
        createAvailability(other, DayOfWeek.MONDAY, 14, 16);

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreate(other, monday(), 14, 15))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("학원 상담 가능 시간 밖의 상담 요청은 실패한다")
    void requestMustBeInsideAvailability() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-unavailable@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 15);

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreate(fixture, monday(), 15, 16))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 자녀/학원/날짜/시간의 REQUESTED 상담 요청은 중복 생성할 수 없다")
    void duplicatedRequestedConsultationIsRejected() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-duplicate@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);
        createRequest(fixture, monday(), 14, 15);

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreate(fixture, monday(), 14, 15))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("ACADEMY가 자기 학원 상담 요청 목록을 조회할 수 있다")
    void academyCanListOwnConsultationRequests() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-list@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);
        createRequest(fixture, monday(), 14, 15);

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$.data[0].parentPhone").value(fixture.parent().getPhone()));
    }

    @Test
    @DisplayName("ACADEMY 상담 요청 목록은 from/to 날짜로 필터링할 수 있다")
    void academyCanFilterConsultationRequestsByDateRange() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-filter@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 17);
        createRequest(fixture, monday(), 14, 15);
        createRequest(fixture, monday().plusWeeks(1), 15, 16);

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .queryParam("from", monday().toString())
                        .queryParam("to", monday().toString())
                        .queryParam("studentProfileId", String.valueOf(fixture.studentProfileId()))
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.studentProfileId()))
                .andExpect(jsonPath("$.data[0].requestedDate").value(monday().toString()));
    }

    @Test
    @DisplayName("ACADEMY 상담 요청 목록은 from 또는 to 단독 날짜 필터로 조회할 수 있다")
    void academyCanFilterConsultationRequestsBySingleDateBoundary() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-single-date-filter@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 17);
        createRequest(fixture, monday(), 14, 15);
        createRequest(fixture, monday().plusWeeks(1), 15, 16);

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .queryParam("from", monday().plusWeeks(1).toString())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].requestedDate").value(monday().plusWeeks(1).toString()));

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .queryParam("to", monday().toString())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].requestedDate").value(monday().toString()));
    }

    @Test
    @DisplayName("ACADEMY 상담 요청 목록은 status와 studentProfileId로 필터링할 수 있다")
    void academyCanFilterConsultationRequestsByStatusAndStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-status-student-filter@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 17);
        Long requestId = createRequest(fixture, monday(), 14, 15);

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/approve", requestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "확인했습니다."))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/academies/me/consultation-requests")
                        .queryParam("status", "APPROVED")
                        .queryParam("studentProfileId", String.valueOf(fixture.studentProfileId()))
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.studentProfileId()));
    }

    @Test
    @DisplayName("ACADEMY가 상담 요청을 승인/거절/완료 처리할 수 있다")
    void academyCanProcessConsultationRequests() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-process@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 17);
        Long approveRequestId = createRequest(fixture, monday(), 14, 15);
        Long rejectRequestId = createRequest(fixture, monday(), 15, 16);

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/approve", approveRequestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "확인했습니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/complete", approveRequestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "완료했습니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/reject", rejectRequestId)
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "일정 불가"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("다른 학원은 상담 요청을 처리할 수 없다")
    void otherAcademyCannotProcessConsultationRequest() throws Exception {
        ConsultationFixture owner = consultationFixture("consult-owner@ringdu.com");
        ConsultationFixture other = consultationFixture("consult-other-academy@ringdu.com");
        createAvailability(owner, DayOfWeek.MONDAY, 14, 16);
        Long requestId = createRequest(owner, monday(), 14, 15);

        mockMvc.perform(post("/api/academies/me/consultation-requests/{requestId}/approve", requestId)
                        .header("Authorization", "Bearer " + other.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("memo", "확인"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TEACHER가 담당 학생 상담 메모를 작성하고 조회할 수 있다")
    void teacherCanCreateAndListAssignedStudentMemo() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-teacher-memo@ringdu.com");

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(fixture.studentProfileId(), null, "학습 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentProfileId").value(fixture.studentProfileId()))
                .andExpect(jsonPath("$.data.academyId").value(fixture.academy().getId()))
                .andExpect(jsonPath("$.data.academyName").value(fixture.academy().getName()))
                .andExpect(jsonPath("$.data.writerUserId").value(fixture.teacher().getId()))
                .andExpect(jsonPath("$.data.writerRole").value("TEACHER"))
                .andExpect(jsonPath("$.data.writerName").value(fixture.teacher().getName()));

        mockMvc.perform(get("/api/teacher/consultation-memos")
                        .queryParam("studentProfileId", String.valueOf(fixture.studentProfileId()))
                        .header("Authorization", "Bearer " + fixture.teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].academyName").value(fixture.academy().getName()))
                .andExpect(jsonPath("$.data[0].title").value("학습 상담"));
    }

    @Test
    @DisplayName("TEACHER가 담당 학생의 ACADEMY 작성 메모까지 조회할 수 있다")
    void teacherCanListAcademyMemoForAssignedStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-teacher-academy-memo@ringdu.com");

        mockMvc.perform(post("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(null, null, "학원 작성 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerRole").value("ACADEMY"));

        mockMvc.perform(get("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].writerRole").value("ACADEMY"))
                .andExpect(jsonPath("$.data[0].title").value("학원 작성 상담"));
    }

    @Test
    @DisplayName("TEACHER는 담당하지 않는 학생 상담 메모를 작성할 수 없다")
    void teacherCannotCreateMemoForUnassignedStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-teacher-forbidden@ringdu.com");
        User otherStudentUser = saveUser("unassigned-student-consult@ringdu.com", Role.STUDENT);
        StudentProfile otherStudent = createStudent(new AcademyContext(fixture.academyToken(), fixture.academy()), otherStudentUser.getId());

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(otherStudent.getId(), null, "권한 없는 상담"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TEACHER는 본인이 작성한 상담 메모만 수정할 수 있다")
    void teacherCanUpdateOwnMemoOnly() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-teacher-update@ringdu.com");

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(fixture.studentProfileId(), null, "수정 전 상담"))))
                .andExpect(status().isOk());

        Long memoId = consultationMemoRepository.findAll().stream()
                .reduce((first, second) -> second)
                .orElseThrow()
                .getId();

        mockMvc.perform(put("/api/teacher/consultation-memos/{memoId}", memoId)
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoUpdate("수정 후 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정 후 상담"));

        User otherTeacher = saveUser("other-teacher-update-consult@ringdu.com", Role.TEACHER);
        mockMvc.perform(put("/api/teacher/consultation-memos/{memoId}", memoId)
                        .header("Authorization", "Bearer " + accessToken(otherTeacher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoUpdate("타인 수정"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ACADEMY가 자기 학원 학생 상담 메모를 작성하고 선생님 작성 메모까지 조회할 수 있다")
    void academyCanCreateAndListTeacherMemoForOwnStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-memo@ringdu.com");

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(fixture.studentProfileId(), null, "선생님 상담"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(null, null, "학원 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerRole").value("ACADEMY"));

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.studentProfileId()));
    }

    @Test
    @DisplayName("ACADEMY가 상담 요청에 연결된 상담 메모를 작성하고 학생별 조회에 포함된다")
    void academyCanCreateRequestLinkedMemo() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-academy-linked-memo@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);
        Long requestId = createRequest(fixture, monday(), 14, 15);

        mockMvc.perform(post("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(null, requestId, "요청 연결 학원 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultationRequestId").value(requestId))
                .andExpect(jsonPath("$.data.writerRole").value("ACADEMY"));

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].consultationRequestId").value(requestId));
    }

    @Test
    @DisplayName("TEACHER가 담당 학생 상담 요청 목록을 조회하고 요청 연결 메모를 작성할 수 있다")
    void teacherCanListRequestsAndCreateLinkedMemoForAssignedStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-teacher-linked-request@ringdu.com");
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 16);
        Long requestId = createRequest(fixture, monday(), 14, 15);

        mockMvc.perform(get("/api/teacher/consultation-memos/requests")
                        .queryParam("studentProfileId", String.valueOf(fixture.studentProfileId()))
                        .header("Authorization", "Bearer " + fixture.teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].consultationRequestId").value(requestId));

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + fixture.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(fixture.studentProfileId(), requestId, "요청 연결 선생님 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultationRequestId").value(requestId));
    }

    @Test
    @DisplayName("다른 학원은 학생 상담 메모를 조회하거나 작성할 수 없다")
    void otherAcademyCannotAccessConsultationMemo() throws Exception {
        ConsultationFixture owner = consultationFixture("consult-memo-owner@ringdu.com");
        ConsultationFixture other = consultationFixture("consult-memo-other@ringdu.com");

        mockMvc.perform(get("/api/academies/me/students/{studentProfileId}/consultation-memos", owner.studentProfileId())
                        .header("Authorization", "Bearer " + other.academyToken()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/academies/me/students/{studentProfileId}/consultation-memos", owner.studentProfileId())
                        .header("Authorization", "Bearer " + other.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(null, null, "다른 학원 상담"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상담 요청 연결 메모는 해당 학원과 학생 상담 요청만 허용한다")
    void memoConsultationRequestLinkMustMatchAcademyAndStudent() throws Exception {
        ConsultationFixture owner = consultationFixture("consult-memo-link-owner@ringdu.com");
        ConsultationFixture other = consultationFixture("consult-memo-link-other@ringdu.com");
        createAvailability(owner, DayOfWeek.MONDAY, 14, 16);
        createAvailability(other, DayOfWeek.MONDAY, 14, 16);
        Long ownerRequestId = createRequest(owner, monday(), 14, 15);
        Long otherRequestId = createRequest(other, monday(), 14, 15);

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + owner.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(owner.studentProfileId(), ownerRequestId, "요청 연결 상담"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultationRequestId").value(ownerRequestId));

        mockMvc.perform(post("/api/teacher/consultation-memos")
                        .header("Authorization", "Bearer " + owner.teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(owner.studentProfileId(), otherRequestId, "잘못된 요청 연결"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상담 요청 ID와 학생 ID가 불일치하면 메모 저장에 실패한다")
    void memoStudentMustMatchConsultationRequestStudent() throws Exception {
        ConsultationFixture fixture = consultationFixture("consult-memo-student-mismatch@ringdu.com");
        User secondParent = saveUser("second-parent-mismatch@ringdu.com", Role.PARENT);
        User secondStudentUser = saveUser("second-student-mismatch@ringdu.com", Role.STUDENT);
        StudentProfile secondStudent = createStudent(new AcademyContext(fixture.academyToken(), fixture.academy()), secondStudentUser.getId());
        parentStudentRelationRepository.save(ParentStudentRelation.create(secondParent, secondStudentUser));
        createAvailability(fixture, DayOfWeek.MONDAY, 14, 17);

        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + accessToken(secondParent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConsultationRequestCreateRequest(
                                fixture.academy().getId(),
                                secondStudent.getId(),
                                fixture.teacher().getId(),
                                monday(),
                                LocalTime.of(14, 0),
                                LocalTime.of(15, 0),
                                ConsultationTopic.STUDY,
                                "두 번째 학생 상담 요청입니다."
                        ))))
                .andExpect(status().isOk());
        Long secondRequestId = consultationRequestRepository.findAllByParentUserIdOrderByCreatedAtDescIdDesc(secondParent.getId())
                .stream()
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(post("/api/academies/me/students/{studentProfileId}/consultation-memos", fixture.studentProfileId())
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoCreate(null, secondRequestId, "학생 불일치 상담"))))
                .andExpect(status().isForbidden());
    }

    private Long createAvailability(ConsultationFixture fixture, DayOfWeek dayOfWeek, int startHour, int endHour) throws Exception {
        mockMvc.perform(post("/api/academies/me/consultation-availability")
                        .header("Authorization", "Bearer " + fixture.academyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest(dayOfWeek, startHour, endHour))))
                .andExpect(status().isOk());
        return fixtureLastAvailabilityId(fixture.academy().getId());
    }

    private Long createRequest(ConsultationFixture fixture, LocalDate date, int startHour, int endHour) throws Exception {
        mockMvc.perform(post("/api/parent/consultation-requests")
                        .header("Authorization", "Bearer " + fixture.parentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreate(fixture, date, startHour, endHour))))
                .andExpect(status().isOk());
        return consultationRequestRepository.findAllByParentUserIdOrderByCreatedAtDescIdDesc(fixture.parent().getId())
                .stream()
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private Long fixtureLastAvailabilityId(Long academyId) {
        return consultationAvailabilityRepository.findAllByAcademyIdOrderByDayOfWeekAscStartTimeAscIdAsc(academyId)
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow()
                .getId();
    }

    private ConsultationAvailabilityRequest availabilityRequest(DayOfWeek dayOfWeek, int startHour, int endHour) {
        return new ConsultationAvailabilityRequest(
                dayOfWeek,
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0),
                ConsultationType.ALL
        );
    }

    private ConsultationRequestCreateRequest requestCreate(
            ConsultationFixture fixture,
            LocalDate requestedDate,
            int startHour,
            int endHour
    ) {
        return new ConsultationRequestCreateRequest(
                fixture.academy().getId(),
                fixture.studentProfileId(),
                fixture.teacher().getId(),
                requestedDate,
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0),
                ConsultationTopic.STUDY,
                "수학 학습 상태 상담을 요청합니다."
        );
    }

    private Map<String, Object> memoCreate(Long studentProfileId, Long consultationRequestId, String title) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("studentProfileId", studentProfileId);
        payload.put("consultationRequestId", consultationRequestId);
        payload.put("title", title);
        payload.put("content", "최근 수학 문제 풀이 속도가 느려져 원인을 확인했습니다.");
        payload.put("nextAction", "다음 수업에서 오답 유형을 다시 확인합니다.");
        payload.put("consultationDate", monday().toString());
        return payload;
    }

    private Map<String, Object> memoUpdate(String title) {
        return Map.of(
                "title", title,
                "content", "수정된 상담 내용입니다.",
                "nextAction", "수정된 다음 조치입니다.",
                "consultationDate", monday().toString()
        );
    }

    private ConsultationFixture consultationFixture(String email) {
        AcademyContext academyContext = academyContext(email);
        User teacher = createConnectedTeacher(academyContext, "teacher-" + email);
        User parent = saveUser("parent-" + email, Role.PARENT);
        User studentUser = saveUser("student-" + email, Role.STUDENT);
        StudentProfile studentProfile = createStudent(academyContext, studentUser.getId());
        parentStudentRelationRepository.save(ParentStudentRelation.create(parent, studentUser));
        AcademyClass academyClass = classRepository.save(AcademyClass.create(
                academyContext.academy().getId(),
                1L,
                teacher.getId(),
                "중등 수학 A반",
                AcademyClassDayOfWeek.MONDAY,
                LocalTime.of(16, 0),
                LocalTime.of(17, 30),
                "테스트 메모"
        ));
        classStudentRepository.save(AcademyClassStudent.create(academyClass.getId(), studentProfile.getId()));
        return new ConsultationFixture(
                academyContext.token(),
                accessToken(parent),
                academyContext.academy(),
                parent,
                teacher,
                accessToken(teacher),
                studentProfile.getId()
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

    private User createConnectedTeacher(AcademyContext context, String email) {
        User teacher = saveUser(email, Role.TEACHER);
        academyMemberRepository.save(AcademyMember.createTeacher(context.academy(), teacher));
        return teacher;
    }

    private StudentProfile createStudent(AcademyContext context, Long studentUserId) {
        return studentProfileRepository.save(StudentProfile.builder()
                .academyId(context.academy().getId())
                .userId(studentUserId)
                .name("김학생")
                .school("동신중")
                .grade("2")
                .phone("010-0000-0000")
                .guardianPhone("010-1111-1111")
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

    private String accessToken(User user) {
        return jwtTokenProvider.createAccessToken(user);
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
        return "010-77%02d-%04d".formatted(suffix / 100, suffix % 10000);
    }

    private LocalDate monday() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private record AcademyContext(String token, Academy academy) {
    }

    private record ConsultationFixture(
            String academyToken,
            String parentToken,
            Academy academy,
            User parent,
            User teacher,
            String teacherToken,
            Long studentProfileId
    ) {
    }
}
