package com.ringdu.server.billing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StudentBillingSettingRequest(
        @NotNull @PositiveOrZero Long monthlyTuition,
        @NotNull @Min(1) @Max(28) Integer dueDay,
        @Size(max = 1000) String memo
) {
}
