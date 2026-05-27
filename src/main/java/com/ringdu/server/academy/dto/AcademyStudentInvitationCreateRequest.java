package com.ringdu.server.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcademyStudentInvitationCreateRequest(
        Long studentProfileId,
        @NotNull Long receiverUserId,
        @Email String receiverEmail,
        String receiverPhone,
        String message
) {
}
