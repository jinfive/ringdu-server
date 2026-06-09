package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingInvoice;
import com.ringdu.server.billing.entity.StudentBillingInvoiceStatus;
import java.util.List;

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

    public static StudentBillingSummaryResponse fromInvoices(
            String billingMonth,
            List<StudentBillingInvoice> invoices,
            boolean hasRegularInvoice,
            boolean canGenerateCurrentMonth
    ) {
        List<StudentBillingInvoice> activeInvoices = invoices.stream()
                .filter(invoice -> invoice.getStatus() != StudentBillingInvoiceStatus.CANCELED)
                .toList();
        if (activeInvoices.isEmpty()) {
            if (invoices.isEmpty()) {
                return withoutInvoice(billingMonth, canGenerateCurrentMonth);
            }
            return new StudentBillingSummaryResponse(
                    billingMonth, "CANCELED", "취소", 0L, 0L, 0L,
                    hasRegularInvoice, canGenerateCurrentMonth
            );
        }

        long amount = activeInvoices.stream().mapToLong(StudentBillingInvoice::getAmount).sum();
        long paidAmount = activeInvoices.stream().mapToLong(StudentBillingInvoice::getPaidAmount).sum();
        StudentBillingInvoiceStatus status = paidAmount == 0
                ? StudentBillingInvoiceStatus.UNPAID
                : paidAmount < amount ? StudentBillingInvoiceStatus.PARTIAL : StudentBillingInvoiceStatus.PAID;
        return new StudentBillingSummaryResponse(
                billingMonth,
                status.name(),
                status.getLabel(),
                amount,
                paidAmount,
                Math.max(amount - paidAmount, 0L),
                hasRegularInvoice,
                canGenerateCurrentMonth
        );
    }
}
