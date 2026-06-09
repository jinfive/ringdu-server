package com.ringdu.server.billing.dto;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.entity.StudentBillingInvoiceStatus;
import com.ringdu.server.billing.entity.StudentBillingType;
import com.ringdu.server.student.entity.StudentProfile;
import java.time.LocalDate;

public record StudentBillingInquiryInvoiceResponse(
        Long billingId,
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName,
        StudentBillingType billingType,
        String billingTypeLabel,
        String billingTitle,
        String billingMonth,
        String billingPeriodStartMonth,
        String billingPeriodEndMonth,
        LocalDate issuedDate,
        LocalDate dueDate,
        Long amount,
        Long paidAmount,
        Long unpaidAmount,
        StudentBillingInvoiceStatus status,
        String statusLabel,
        String memo
) {
    public static StudentBillingInquiryInvoiceResponse from(
            StudentBillingInvoice invoice,
            StudentProfile student,
            Academy academy
    ) {
        return new StudentBillingInquiryInvoiceResponse(
                invoice.getId(),
                student.getId(),
                student.getName(),
                academy.getId(),
                academy.getName(),
                invoice.getEffectiveBillingType(),
                invoice.getEffectiveBillingType().getLabel(),
                createTitle(invoice),
                invoice.getBillingMonth(),
                invoice.getEffectiveBillingPeriodStartMonth(),
                invoice.getEffectiveBillingPeriodEndMonth(),
                invoice.getIssuedDate(),
                invoice.getDueDate(),
                invoice.getAmount(),
                invoice.getPaidAmount(),
                invoice.getUnpaidAmount(),
                invoice.getStatus(),
                invoice.getStatus().getLabel(),
                invoice.getMemo()
        );
    }

    private static String createTitle(StudentBillingInvoice invoice) {
        String start = formatMonth(invoice.getEffectiveBillingPeriodStartMonth());
        String end = formatMonth(invoice.getEffectiveBillingPeriodEndMonth());
        String period = start.equals(end) ? start : start + "~" + end;
        return period + " " + invoice.getEffectiveBillingType().getLabel();
    }

    private static String formatMonth(String value) {
        String[] parts = value.split("-");
        return parts[0] + "년 " + Integer.parseInt(parts[1]) + "월";
    }
}
