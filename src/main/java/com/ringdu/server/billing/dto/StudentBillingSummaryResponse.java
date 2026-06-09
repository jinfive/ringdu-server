package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingInvoice;

public record StudentBillingSummaryResponse(
        String billingMonth,
        String status,
        String statusLabel,
        Long amount,
        Long paidAmount,
        Long unpaidAmount,
        boolean hasInvoice,
        boolean canGenerateCurrentMonth
) {
    public static StudentBillingSummaryResponse withoutInvoice(String billingMonth, boolean canGenerateCurrentMonth) {
        return new StudentBillingSummaryResponse(
                billingMonth,
                "NOT_ISSUED",
                "미청구",
                0L,
                0L,
                0L,
                false,
                canGenerateCurrentMonth
        );
    }

    public static StudentBillingSummaryResponse from(StudentBillingInvoice invoice) {
        return new StudentBillingSummaryResponse(
                invoice.getBillingMonth(),
                invoice.getStatus().name(),
                invoice.getStatus().getLabel(),
                invoice.getAmount(),
                invoice.getPaidAmount(),
                invoice.getUnpaidAmount(),
                true,
                false
        );
    }
}
