package com.ringdu.server.billing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.entity.StudentBillingType;
import com.ringdu.server.billing.repository.StudentBillingInvoiceRepository;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentBillingInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ParentStudentRelationRepository relationRepository;

    @Autowired
    private StudentBillingInvoiceRepository invoiceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("학부모는 활성 연결 자녀의 청구 내역과 요약을 조회할 수 있다")
    void parentCanReadConnectedChildBilling() throws Exception {
        InquiryFixture fixture = fixture("parent-connected");
        relationRepository.save(ParentStudentRelation.create(fixture.parent(), fixture.student()));
        createInvoice(fixture.profile(), fixture.academy(), 900_000L, 300_000L);

        mockMvc.perform(get(parentInvoicesPath(fixture.profile().getId()))
                        .param("year", "2026")
                        .header("Authorization", bearer(fixture.parentToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentName").value("학생1"))
                .andExpect(jsonPath("$.data[0].academyName").value("테스트 학원"))
                .andExpect(jsonPath("$.data[0].billingTitle").value("2026년 8월~2026년 10월 3개월 선납"))
                .andExpect(jsonPath("$.data[0].status").value("PARTIAL"));

        mockMvc.perform(get(parentSummaryPath(fixture.profile().getId()))
                        .param("year", "2026")
                        .param("month", "8")
                        .header("Authorization", bearer(fixture.parentToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(900000))
                .andExpect(jsonPath("$.data.paidAmount").value(300000))
                .andExpect(jsonPath("$.data.unpaidAmount").value(600000))
                .andExpect(jsonPath("$.data.unpaidCount").value(1));
    }

    @Test
    @DisplayName("학부모는 연결되지 않은 학생의 청구를 조회할 수 없다")
    void parentCannotReadUnconnectedStudentBilling() throws Exception {
        InquiryFixture fixture = fixture("parent-unconnected");

        mockMvc.perform(get(parentInvoicesPath(fixture.profile().getId()))
                        .header("Authorization", bearer(fixture.parentToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("학생은 본인 프로필의 청구 내역만 조회한다")
    void studentCanReadOnlyOwnBilling() throws Exception {
        InquiryFixture fixture = fixture("student-own");
        createInvoice(fixture.profile(), fixture.academy(), 300_000L, 0L);
        InquiryFixture other = fixture("student-other");
        createInvoice(other.profile(), other.academy(), 500_000L, 0L);

        mockMvc.perform(get("/api/student/billing/invoices")
                        .param("year", "2026")
                        .header("Authorization", bearer(fixture.studentToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentProfileId").value(fixture.profile().getId()))
                .andExpect(jsonPath("$.data[0].amount").value(300000));
    }

    @Test
    @DisplayName("학생 프로필이 없는 학생 계정은 빈 청구 목록을 조회한다")
    void studentWithoutProfileGetsEmptyBilling() throws Exception {
        User student = createUser("student-empty@ringdu.com", "학생", Role.STUDENT);

        mockMvc.perform(get("/api/student/billing/invoices")
                        .header("Authorization", bearer(jwtTokenProvider.createAccessToken(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("학부모와 학생 청구 조회 경로는 관리 메서드를 제공하지 않는다")
    void inquiryEndpointsDoNotProvideBillingActions() throws Exception {
        InquiryFixture fixture = fixture("billing-readonly");
        relationRepository.save(ParentStudentRelation.create(fixture.parent(), fixture.student()));

        mockMvc.perform(post(parentInvoicesPath(fixture.profile().getId()))
                        .header("Authorization", bearer(fixture.parentToken())))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/student/billing/invoices")
                        .header("Authorization", bearer(fixture.studentToken())))
                .andExpect(status().isMethodNotAllowed());
    }

    private InquiryFixture fixture(String prefix) {
        User academyUser = createUser(prefix + "-academy@ringdu.com", "학원", Role.ACADEMY);
        Academy academy = academyRepository.save(Academy.create(
                academyUser, "테스트 학원", "김대표", "02-1234-5678", "06123", "서울", "101호"
        ));
        User parent = createUser(prefix + "-parent@ringdu.com", "학부모", Role.PARENT);
        User student = createUser(prefix + "-student@ringdu.com", "학생1", Role.STUDENT);
        StudentProfile profile = studentProfileRepository.save(StudentProfile.builder()
                .academyId(academy.getId())
                .userId(student.getId())
                .name("학생1")
                .status(StudentStatus.ACTIVE)
                .build());
        return new InquiryFixture(
                academy,
                parent,
                student,
                profile,
                jwtTokenProvider.createAccessToken(parent),
                jwtTokenProvider.createAccessToken(student)
        );
    }

    private User createUser(String email, String name, Role role) {
        return userRepository.save(User.createLocalUser(
                email, passwordEncoder.encode("password1234"), name, "010-1234-5678", role
        ));
    }

    private void createInvoice(StudentProfile profile, Academy academy, long amount, long paidAmount) {
        StudentBillingInvoice invoice = invoiceRepository.save(new StudentBillingInvoice(
                academy.getId(),
                profile.getId(),
                StudentBillingType.PREPAID,
                "2026-08",
                "2026-08",
                "2026-10",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                amount,
                "3개월 선납"
        ));
        if (paidAmount > 0) {
            invoice.addPayment(paidAmount);
        }
    }

    private String parentInvoicesPath(Long studentProfileId) {
        return "/api/parent/children/" + studentProfileId + "/billing/invoices";
    }

    private String parentSummaryPath(Long studentProfileId) {
        return "/api/parent/children/" + studentProfileId + "/billing/summary";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record InquiryFixture(
            Academy academy,
            User parent,
            User student,
            StudentProfile profile,
            String parentToken,
            String studentToken
    ) {
    }
}
