package com.ringdu.server.parentstudent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentParentInvitationCreateRequest(
        @NotBlank(message = "보호자 이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String parentEmail,

        @NotBlank(message = "보호자 전화번호는 필수입니다.")
        @Size(max = 30, message = "보호자 전화번호는 30자 이하여야 합니다.")
        String parentPhone,

        @Size(max = 500, message = "초대 메시지는 500자 이하여야 합니다.")
        String message
) {
}
