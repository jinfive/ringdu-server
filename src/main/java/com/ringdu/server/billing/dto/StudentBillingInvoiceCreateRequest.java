package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentBillingInvoiceCreateRequest(
        @NotNull StudentBillingType billingType,
        @NotNull @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])") String billingPeriodStartMonth,
        @NotNull @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])") String billingPeriodEndMonth,
        @NotNull LocalDate dueDate,
        @NotNull @PositiveOrZero Long amount,
        @Size(max = 1000) String memo
) {
}
