package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.entity.StudentBillingInvoiceStatus;
import com.ringdu.server.billing.entity.StudentBillingType;
import java.time.LocalDate;

public record StudentBillingInvoiceResponse(
        Long billingId,
        Long studentProfileId,
        StudentBillingType billingType,
        String billingTypeLabel,
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
    public static StudentBillingInvoiceResponse from(StudentBillingInvoice invoice) {
        return new StudentBillingInvoiceResponse(
                invoice.getId(),
                invoice.getStudentProfileId(),
                invoice.getEffectiveBillingType(),
                invoice.getEffectiveBillingType().getLabel(),
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
}
