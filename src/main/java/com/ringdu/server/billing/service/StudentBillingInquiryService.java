package com.ringdu.server.billing.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.billing.dto.StudentBillingInquiryInvoiceResponse;
import com.ringdu.server.billing.dto.StudentBillingInquirySummaryResponse;
import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.repository.StudentBillingInvoiceRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentBillingInquiryService {

    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentRelationRepository relationRepository;
    private final StudentBillingInvoiceRepository invoiceRepository;
    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    public List<StudentBillingInquiryInvoiceResponse> getParentInvoices(
            Long parentUserId,
            Long studentProfileId,
            Integer year
    ) {
        StudentProfile profile = getParentChildProfile(parentUserId, studentProfileId);
        return getInvoices(List.of(profile), year, null);
    }

    @Transactional(readOnly = true)
    public StudentBillingInquirySummaryResponse getParentSummary(
            Long parentUserId,
            Long studentProfileId,
            Integer year,
            Integer month
    ) {
        StudentProfile profile = getParentChildProfile(parentUserId, studentProfileId);
        YearMonth billingMonth = toYearMonth(year, month);
        return StudentBillingInquirySummaryResponse.from(
                billingMonth.toString(),
                getInvoices(List.of(profile), year, month)
        );
    }

    @Transactional(readOnly = true)
    public List<StudentBillingInquiryInvoiceResponse> getStudentInvoices(Long studentUserId, Integer year) {
        return getInvoices(getStudentProfiles(studentUserId), year, null);
    }

    @Transactional(readOnly = true)
    public StudentBillingInquirySummaryResponse getStudentSummary(
            Long studentUserId,
            Integer year,
            Integer month
    ) {
        YearMonth billingMonth = toYearMonth(year, month);
        return StudentBillingInquirySummaryResponse.from(
                billingMonth.toString(),
                getInvoices(getStudentProfiles(studentUserId), year, month)
        );
    }

    private List<StudentBillingInquiryInvoiceResponse> getInvoices(
            List<StudentProfile> profiles,
            Integer year,
            Integer month
    ) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        Map<Long, StudentProfile> profilesById = profiles.stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
        Map<Long, Academy> academiesById = academyRepository.findAllById(
                        profiles.stream().map(StudentProfile::getAcademyId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));

        return invoiceRepository.findAllByStudentProfileIdInOrderByBillingMonthDescIdDesc(
                        profiles.stream().map(StudentProfile::getId).toList()
                ).stream()
                .filter(invoice -> year == null || invoice.getBillingMonth().startsWith(year + "-"))
                .filter(invoice -> month == null || invoice.getBillingMonth().endsWith("-" + String.format("%02d", month)))
                .map(invoice -> toResponse(invoice, profilesById, academiesById))
                .toList();
    }

    private StudentBillingInquiryInvoiceResponse toResponse(
            StudentBillingInvoice invoice,
            Map<Long, StudentProfile> profilesById,
            Map<Long, Academy> academiesById
    ) {
        StudentProfile profile = profilesById.get(invoice.getStudentProfileId());
        Academy academy = academiesById.get(invoice.getAcademyId());
        if (profile == null || academy == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return StudentBillingInquiryInvoiceResponse.from(invoice, profile, academy);
    }

    private StudentProfile getParentChildProfile(Long parentUserId, Long studentProfileId) {
        StudentProfile profile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (profile.getStatus() != StudentStatus.ACTIVE
                || profile.getUserId() == null
                || !relationRepository.existsByParentIdAndStudentIdAndStatus(
                        parentUserId, profile.getUserId(), ParentStudentRelationStatus.ACTIVE
                )) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return profile;
    }

    private List<StudentProfile> getStudentProfiles(Long studentUserId) {
        return studentProfileRepository.findAllByUserIdAndStatusOrderByNameAscIdAsc(
                studentUserId, StudentStatus.ACTIVE
        );
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
