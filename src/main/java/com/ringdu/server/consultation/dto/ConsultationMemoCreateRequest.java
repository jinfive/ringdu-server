package com.ringdu.server.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ConsultationMemoCreateRequest(
        Long studentProfileId,
        Long consultationRequestId,
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        String nextAction,
        @NotNull LocalDate consultationDate
) {
}
