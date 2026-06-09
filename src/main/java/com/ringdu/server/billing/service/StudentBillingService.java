package com.ringdu.server.billing.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.billing.dto.StudentBillingEnsureCurrentResponse;
import com.ringdu.server.billing.dto.StudentBillingInvoiceResponse;
import com.ringdu.server.billing.dto.StudentBillingInvoiceUpdateRequest;
import com.ringdu.server.billing.dto.StudentBillingPaymentRequest;
import com.ringdu.server.billing.dto.StudentBillingSettingRequest;
import com.ringdu.server.billing.dto.StudentBillingSettingResponse;
import com.ringdu.server.billing.dto.StudentBillingSummaryResponse;
import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.entity.StudentBillingPayment;
import com.ringdu.server.billing.entity.StudentBillingSetting;
import com.ringdu.server.billing.repository.StudentBillingInvoiceRepository;
import com.ringdu.server.billing.repository.StudentBillingPaymentRepository;
import com.ringdu.server.billing.repository.StudentBillingSettingRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentProfileRepository;
import java.time.DateTimeException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentBillingService {

    private final AcademyRepository academyRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentBillingSettingRepository settingRepository;
    private final StudentBillingInvoiceRepository invoiceRepository;
    private final StudentBillingPaymentRepository paymentRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public StudentBillingSettingResponse getSetting(Long academyUserId, Long studentProfileId) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        return settingRepository.findByAcademyIdAndStudentProfileId(owner.academyId(), owner.studentProfileId())
                .map(StudentBillingSettingResponse::from)
                .orElseGet(() -> StudentBillingSettingResponse.empty(owner.studentProfileId()));
    }

    @Transactional
    public StudentBillingSettingResponse saveSetting(
            Long academyUserId,
            Long studentProfileId,
            StudentBillingSettingRequest request
    ) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        StudentBillingSetting setting = settingRepository
                .findByAcademyIdAndStudentProfileId(owner.academyId(), owner.studentProfileId())
                .orElseGet(() -> new StudentBillingSetting(
                        owner.academyId(),
                        owner.studentProfileId(),
                        request.monthlyTuition(),
                        request.dueDay(),
                        normalizeMemo(request.memo())
                ));

        setting.update(request.monthlyTuition(), request.dueDay(), normalizeMemo(request.memo()));
        return StudentBillingSettingResponse.from(settingRepository.save(setting));
    }

    @Transactional(readOnly = true)
    public StudentBillingSummaryResponse getSummary(
            Long academyUserId,
            Long studentProfileId,
            Integer year,
            Integer month
    ) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        YearMonth billingMonth = toYearMonth(year, month);
        return invoiceRepository
                .findByAcademyIdAndStudentProfileIdAndBillingMonth(
                        owner.academyId(),
                        owner.studentProfileId(),
                        billingMonth.toString()
                )
                .map(StudentBillingSummaryResponse::from)
                .orElseGet(() -> StudentBillingSummaryResponse.withoutInvoice(
                        billingMonth.toString(),
                        canGenerate(owner, billingMonth, LocalDate.now(clock))
                ));
    }

    @Transactional(readOnly = true)
    public List<StudentBillingInvoiceResponse> getInvoices(
            Long academyUserId,
            Long studentProfileId,
            Integer year,
            Integer month
    ) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        return invoiceRepository
                .findAllByAcademyIdAndStudentProfileIdOrderByBillingMonthDesc(owner.academyId(), owner.studentProfileId())
                .stream()
                .filter(invoice -> year == null || invoice.getBillingMonth().startsWith(year + "-"))
                .filter(invoice -> month == null || invoice.getBillingMonth().endsWith("-" + String.format("%02d", month)))
                .map(StudentBillingInvoiceResponse::from)
                .toList();
    }

    @Transactional
    public StudentBillingEnsureCurrentResponse ensureCurrentInvoice(Long academyUserId, Long studentProfileId) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        LocalDate today = LocalDate.now(clock);
        YearMonth currentMonth = YearMonth.from(today);
        String billingMonth = currentMonth.toString();

        var existing = invoiceRepository.findByAcademyIdAndStudentProfileIdAndBillingMonth(
                owner.academyId(),
                owner.studentProfileId(),
                billingMonth
        );
        if (existing.isPresent()) {
            return new StudentBillingEnsureCurrentResponse(
                    false,
                    "이미 이번 달 청구가 생성되어 있습니다.",
                    StudentBillingInvoiceResponse.from(existing.get())
            );
        }

        StudentBillingSetting setting = settingRepository
                .findByAcademyIdAndStudentProfileId(owner.academyId(), owner.studentProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BILLING_SETTING_NOT_FOUND));
        if (today.getDayOfMonth() < setting.getDueDay()) {
            return new StudentBillingEnsureCurrentResponse(false, "아직 수납 기준일 전입니다.", null);
        }

        StudentBillingInvoice invoice = invoiceRepository.save(new StudentBillingInvoice(
                owner.academyId(),
                owner.studentProfileId(),
                billingMonth,
                today,
                currentMonth.atDay(setting.getDueDay()),
                setting.getMonthlyTuition(),
                setting.getMemo()
        ));
        return new StudentBillingEnsureCurrentResponse(
                true,
                currentMonth.getYear() + "년 " + currentMonth.getMonthValue() + "월 청구가 생성되었습니다.",
                StudentBillingInvoiceResponse.from(invoice)
        );
    }

    @Transactional
    public StudentBillingInvoiceResponse updateInvoice(
            Long academyUserId,
            Long studentProfileId,
            Long billingId,
            StudentBillingInvoiceUpdateRequest request
    ) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        StudentBillingInvoice invoice = getOwnedInvoice(owner, billingId);
        invoice.updateAmount(request.amount(), normalizeMemo(request.memo()));
        return StudentBillingInvoiceResponse.from(invoice);
    }

    @Transactional
    public StudentBillingInvoiceResponse addPayment(
            Long academyUserId,
            Long studentProfileId,
            Long billingId,
            StudentBillingPaymentRequest request
    ) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        StudentBillingInvoice invoice = getOwnedInvoice(owner, billingId);
        invoice.addPayment(request.paymentAmount());
        paymentRepository.save(new StudentBillingPayment(
                invoice.getId(),
                owner.academyId(),
                owner.studentProfileId(),
                request.paymentAmount(),
                request.paymentDate(),
                normalizeMemo(request.memo())
        ));
        return StudentBillingInvoiceResponse.from(invoice);
    }

    @Transactional
    public StudentBillingInvoiceResponse cancelInvoice(Long academyUserId, Long studentProfileId, Long billingId) {
        BillingOwner owner = getOwner(academyUserId, studentProfileId);
        StudentBillingInvoice invoice = getOwnedInvoice(owner, billingId);
        invoice.cancel();
        return StudentBillingInvoiceResponse.from(invoice);
    }

    private boolean canGenerate(BillingOwner owner, YearMonth billingMonth, LocalDate today) {
        if (!billingMonth.equals(YearMonth.from(today))) {
            return false;
        }
        return settingRepository.findByAcademyIdAndStudentProfileId(owner.academyId(), owner.studentProfileId())
                .map(setting -> today.getDayOfMonth() >= setting.getDueDay())
                .orElse(false);
    }

    private StudentBillingInvoice getOwnedInvoice(BillingOwner owner, Long billingId) {
        StudentBillingInvoice invoice = invoiceRepository.findById(billingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BILLING_INVOICE_NOT_FOUND));
        if (!invoice.getAcademyId().equals(owner.academyId())
                || !invoice.getStudentProfileId().equals(owner.studentProfileId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return invoice;
    }

    private BillingOwner getOwner(Long academyUserId, Long studentProfileId) {
        Academy academy = academyRepository.findByUserId(academyUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (!student.getAcademyId().equals(academy.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return new BillingOwner(academy.getId(), student.getId());
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeMemo(String memo) {
        return memo == null || memo.isBlank() ? null : memo.trim();
    }

    private record BillingOwner(Long academyId, Long studentProfileId) {
    }
}
