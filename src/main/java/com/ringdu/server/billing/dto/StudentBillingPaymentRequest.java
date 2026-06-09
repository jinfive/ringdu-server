package com.ringdu.server.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentBillingPaymentRequest(
        @NotNull @Positive Long paymentAmount,
        @NotNull LocalDate paymentDate,
        @Size(max = 1000) String memo
) {
}
