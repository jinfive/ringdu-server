package com.ringdu.server.billing.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentBillingControllerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-08-15T01:00:00Z");
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private Clock clock;

    @BeforeEach
    void setUpClock() {
        when(clock.instant()).thenReturn(FIXED_NOW);
        when(clock.getZone()).thenReturn(SEOUL);
    }

    @Test
    @DisplayName("학원은 자기 학생 수납 설정을 저장하고 조회할 수 있다")
    void academyCanSaveBillingSetting() throws Exception {
        BillingFixture fixture = fixture("billing-setting@ringdu.com");

        saveSetting(fixture, 300_000L, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentProfileId").value(fixture.student().getId()))
                .andExpect(jsonPath("$.data.monthlyTuition").value(300000))
                .andExpect(jsonPath("$.data.dueDay").value(1))
                .andExpect(jsonPath("$.data.configured").value(true));

        mockMvc.perform(get(settingPath(fixture.student().getId()))
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memo").value("월 정규 수강료"));
    }

    @Test
    @DisplayName("다른 학원 학생 수납 설정에는 접근할 수 없다")
    void academyCannotAccessOtherAcademyStudent() throws Exception {
        BillingFixture owner = fixture("billing-owner@ringdu.com");
        BillingFixture other = fixture("billing-other@ringdu.com");

        mockMvc.perform(get(settingPath(owner.student().getId()))
                        .header("Authorization", bearer(other.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("수납 기준일이 도래하면 현재 월 청구를 생성하고 조회한다")
    void ensureCurrentInvoiceAfterDueDay() throws Exception {
        BillingFixture fixture = fixture("billing-ensure@ringdu.com");
        saveSetting(fixture, 300_000L, 1).andExpect(status().isOk());

        ensureCurrent(fixture)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generated").value(true))
                .andExpect(jsonPath("$.data.invoice.billingMonth").value("2026-08"))
                .andExpect(jsonPath("$.data.invoice.status").value("UNPAID"));

        mockMvc.perform(get(billingPath(fixture.student().getId()) + "/summary")
                        .param("year", "2026")
                        .param("month", "8")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasInvoice").value(true))
                .andExpect(jsonPath("$.data.unpaidAmount").value(300000));

        mockMvc.perform(get(billingPath(fixture.student().getId()) + "/invoices")
                        .param("year", "2026")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("수납 기준일 전이면 현재 월 청구를 생성하지 않는다")
    void ensureCurrentInvoiceBeforeDueDay() throws Exception {
        BillingFixture fixture = fixture("billing-before-due@ringdu.com");
        saveSetting(fixture, 300_000L, 20).andExpect(status().isOk());

        ensureCurrent(fixture)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generated").value(false))
                .andExpect(jsonPath("$.data.invoice").doesNotExist())
                .andExpect(jsonPath("$.data.message").value("아직 수납 기준일 전입니다."));
    }

    @Test
    @DisplayName("같은 달 청구는 중복 생성하지 않는다")
    void currentInvoiceIsIdempotent() throws Exception {
        BillingFixture fixture = fixture("billing-idempotent@ringdu.com");
        saveSetting(fixture, 300_000L, 1).andExpect(status().isOk());
        Long firstId = ensureInvoiceId(fixture);

        ensureCurrent(fixture)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generated").value(false))
                .andExpect(jsonPath("$.data.invoice.billingId").value(firstId));
    }

    @Test
    @DisplayName("학원은 여러 달 선납 청구를 하나의 청구서로 생성할 수 있다")
    void academyCanCreatePrepaidInvoice() throws Exception {
        BillingFixture fixture = fixture("billing-prepaid@ringdu.com");

        createManualInvoice(fixture, "PREPAID", "2026-08", "2026-10", 900_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.billingType").value("PREPAID"))
                .andExpect(jsonPath("$.data.billingTypeLabel").value("3개월 선납"))
                .andExpect(jsonPath("$.data.billingMonth").value("2026-08"))
                .andExpect(jsonPath("$.data.billingPeriodStartMonth").value("2026-08"))
                .andExpect(jsonPath("$.data.billingPeriodEndMonth").value("2026-10"))
                .andExpect(jsonPath("$.data.amount").value(900000))
                .andExpect(jsonPath("$.data.unpaidAmount").value(900000));
    }

    @Test
    @DisplayName("임의 청구는 같은 기간에 여러 건 생성할 수 있다")
    void manualInvoicesCanShareBillingPeriod() throws Exception {
        BillingFixture fixture = fixture("billing-manual-duplicate@ringdu.com");

        createManualInvoice(fixture, "TEXTBOOK", "2026-08", "2026-08", 30_000L)
                .andExpect(status().isOk());
        createManualInvoice(fixture, "TEXTBOOK", "2026-08", "2026-08", 40_000L)
                .andExpect(status().isOk());

        mockMvc.perform(get(billingPath(fixture.student().getId()) + "/invoices")
                        .param("year", "2026")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("청구 시작월은 종료월보다 늦을 수 없다")
    void manualInvoicePeriodMustBeOrdered() throws Exception {
        BillingFixture fixture = fixture("billing-period-invalid@ringdu.com");

        createManualInvoice(fixture, "PREPAID", "2026-10", "2026-08", 900_000L)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("임의 청구가 있어도 이번 달 정규 청구는 한 번만 자동 생성한다")
    void manualInvoiceDoesNotBlockRegularInvoice() throws Exception {
        BillingFixture fixture = fixture("billing-manual-regular@ringdu.com");
        saveSetting(fixture, 300_000L, 1).andExpect(status().isOk());
        createManualInvoice(fixture, "TEXTBOOK", "2026-08", "2026-08", 30_000L)
                .andExpect(status().isOk());

        mockMvc.perform(get(billingPath(fixture.student().getId()) + "/summary")
                        .param("year", "2026")
                        .param("month", "8")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(30000))
                .andExpect(jsonPath("$.data.hasInvoice").value(false));

        ensureCurrent(fixture)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generated").value(true))
                .andExpect(jsonPath("$.data.invoice.billingType").value("REGULAR"));

        mockMvc.perform(get(billingPath(fixture.student().getId()) + "/summary")
                        .param("year", "2026")
                        .param("month", "8")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(330000))
                .andExpect(jsonPath("$.data.hasInvoice").value(true));
        ensureCurrent(fixture)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generated").value(false));
    }

    @Test
    @DisplayName("청구 금액과 메모를 수정할 수 있다")
    void academyCanUpdateInvoiceAmount() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-update@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);

        updateInvoice(fixture, billingId, 320_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(320000))
                .andExpect(jsonPath("$.data.memo").value("보강 포함"));
    }

    @Test
    @DisplayName("청구 금액을 이미 수납한 금액보다 낮게 수정할 수 없다")
    void invoiceAmountCannotBeLessThanPaidAmount() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-lower-amount@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);
        pay(fixture, billingId, 100_000L).andExpect(status().isOk());

        updateInvoice(fixture, billingId, 50_000L)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일부 금액 수납 후 부분수납 상태가 된다")
    void partialPaymentChangesStatus() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-partial@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);

        pay(fixture, billingId, 100_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paidAmount").value(100000))
                .andExpect(jsonPath("$.data.unpaidAmount").value(200000))
                .andExpect(jsonPath("$.data.status").value("PARTIAL"));
    }

    @Test
    @DisplayName("전액 수납 후 완납 상태가 된다")
    void fullPaymentChangesStatus() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-paid@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);

        pay(fixture, billingId, 300_000L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paidAmount").value(300000))
                .andExpect(jsonPath("$.data.unpaidAmount").value(0))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    @DisplayName("미납 청구를 취소할 수 있다")
    void academyCanCancelInvoice() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-cancel@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);

        mockMvc.perform(post(invoicePath(fixture.student().getId(), billingId) + "/cancel")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    @DisplayName("취소한 청구서에는 수납 처리할 수 없다")
    void canceledInvoiceCannotReceivePayment() throws Exception {
        BillingFixture fixture = fixtureWithInvoice("billing-canceled-payment@ringdu.com", 300_000L);
        Long billingId = currentInvoiceId(fixture);
        mockMvc.perform(post(invoicePath(fixture.student().getId(), billingId) + "/cancel")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk());

        pay(fixture, billingId, 100_000L)
                .andExpect(status().isConflict());
    }

    private BillingFixture fixtureWithInvoice(String email, Long amount) throws Exception {
        BillingFixture fixture = fixture(email);
        saveSetting(fixture, amount, 1).andExpect(status().isOk());
        ensureCurrent(fixture).andExpect(status().isOk());
        return fixture;
    }

    private Long currentInvoiceId(BillingFixture fixture) throws Exception {
        MvcResult result = mockMvc.perform(get(billingPath(fixture.student().getId()) + "/invoices")
                        .header("Authorization", bearer(fixture.token())))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.get(0).path("billingId").asLong();
    }

    private Long ensureInvoiceId(BillingFixture fixture) throws Exception {
        MvcResult result = ensureCurrent(fixture).andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("invoice").path("billingId").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions saveSetting(
            BillingFixture fixture,
            Long amount,
            int dueDay
    ) throws Exception {
        return mockMvc.perform(put(settingPath(fixture.student().getId()))
                .header("Authorization", bearer(fixture.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "monthlyTuition", amount,
                        "dueDay", dueDay,
                        "memo", "월 정규 수강료"
                ))));
    }

    private org.springframework.test.web.servlet.ResultActions ensureCurrent(BillingFixture fixture) throws Exception {
        return mockMvc.perform(post(billingPath(fixture.student().getId()) + "/invoices/ensure-current")
                .header("Authorization", bearer(fixture.token())));
    }

    private org.springframework.test.web.servlet.ResultActions createManualInvoice(
            BillingFixture fixture,
            String billingType,
            String startMonth,
            String endMonth,
            Long amount
    ) throws Exception {
        return mockMvc.perform(post(billingPath(fixture.student().getId()) + "/invoices")
                .header("Authorization", bearer(fixture.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "billingType", billingType,
                        "billingPeriodStartMonth", startMonth,
                        "billingPeriodEndMonth", endMonth,
                        "dueDate", "2026-08-01",
                        "amount", amount,
                        "memo", "임의 청구 테스트"
                ))));
    }

    private org.springframework.test.web.servlet.ResultActions updateInvoice(
            BillingFixture fixture,
            Long billingId,
            Long amount
    ) throws Exception {
        return mockMvc.perform(put(invoicePath(fixture.student().getId(), billingId))
                .header("Authorization", bearer(fixture.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "amount", amount,
                        "memo", "보강 포함"
                ))));
    }

    private org.springframework.test.web.servlet.ResultActions pay(
            BillingFixture fixture,
            Long billingId,
            Long paymentAmount
    ) throws Exception {
        return mockMvc.perform(post(invoicePath(fixture.student().getId(), billingId) + "/payments")
                .header("Authorization", bearer(fixture.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "paymentAmount", paymentAmount,
                        "paymentDate", "2026-08-15",
                        "memo", "테스트 수납"
                ))));
    }

    private BillingFixture fixture(String email) {
        User academyUser = userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 학원",
                "010-1234-5678",
                Role.ACADEMY
        ));
        Academy academy = academyRepository.save(Academy.create(
                academyUser,
                "테스트 학원",
                "김대표",
                "02-1234-5678",
                "06123",
                "서울시 강남구",
                "101호"
        ));
        StudentProfile student = studentProfileRepository.save(StudentProfile.builder()
                .academyId(academy.getId())
                .name("학생1")
                .status(StudentStatus.ACTIVE)
                .build());
        return new BillingFixture(jwtTokenProvider.createAccessToken(academyUser), student);
    }

    private String settingPath(Long studentProfileId) {
        return billingPath(studentProfileId) + "/setting";
    }

    private String billingPath(Long studentProfileId) {
        return "/api/academies/me/students/" + studentProfileId + "/billing";
    }

    private String invoicePath(Long studentProfileId, Long billingId) {
        return billingPath(studentProfileId) + "/invoices/" + billingId;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record BillingFixture(String token, StudentProfile student) {
    }
}
