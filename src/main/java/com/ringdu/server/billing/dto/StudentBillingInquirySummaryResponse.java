package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingInvoiceStatus;
import java.util.List;

public record StudentBillingInquirySummaryResponse(
        String billingMonth,
        Long amount,
        Long paidAmount,
        Long unpaidAmount,
        long unpaidCount
) {
    public static StudentBillingInquirySummaryResponse from(
            String billingMonth,
            List<StudentBillingInquiryInvoiceResponse> invoices
    ) {
        List<StudentBillingInquiryInvoiceResponse> activeInvoices = invoices.stream()
                .filter(invoice -> invoice.status() != StudentBillingInvoiceStatus.CANCELED)
                .toList();
        long amount = activeInvoices.stream().mapToLong(StudentBillingInquiryInvoiceResponse::amount).sum();
        long paidAmount = activeInvoices.stream().mapToLong(StudentBillingInquiryInvoiceResponse::paidAmount).sum();
        long unpaidAmount = activeInvoices.stream().mapToLong(StudentBillingInquiryInvoiceResponse::unpaidAmount).sum();
        long unpaidCount = activeInvoices.stream().filter(invoice -> invoice.unpaidAmount() > 0).count();
        return new StudentBillingInquirySummaryResponse(
                billingMonth, amount, paidAmount, unpaidAmount, unpaidCount
        );
    }
}
