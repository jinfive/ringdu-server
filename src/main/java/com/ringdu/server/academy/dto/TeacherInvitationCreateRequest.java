package com.ringdu.server.academy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeacherInvitationCreateRequest(
        @NotBlank(message = "선생님 이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String teacherEmail,

        @NotBlank(message = "선생님 전화번호는 필수입니다.")
        @Size(max = 30, message = "선생님 전화번호는 30자 이하여야 합니다.")
        String teacherPhone,

        @Size(max = 500, message = "초대 메시지는 500자 이하여야 합니다.")
        String message
) {
}
