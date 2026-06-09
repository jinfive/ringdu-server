package com.ringdu.server.billing.dto;

public record StudentBillingEnsureCurrentResponse(
        boolean generated,
        String message,
        StudentBillingInvoiceResponse invoice
) {
}
