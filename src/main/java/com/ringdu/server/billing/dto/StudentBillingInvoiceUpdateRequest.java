package com.ringdu.server.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StudentBillingInvoiceUpdateRequest(
        @NotNull @PositiveOrZero Long amount,
        @Size(max = 1000) String memo
) {
}
