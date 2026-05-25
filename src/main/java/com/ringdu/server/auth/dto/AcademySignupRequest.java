package com.ringdu.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcademySignupRequest(
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        @Size(min = 8, message = "비밀번호 확인은 최소 8자 이상이어야 합니다.")
        String passwordConfirm,

        @NotBlank(message = "학원명은 필수입니다.")
        String academyName,

        @NotBlank(message = "대표자명은 필수입니다.")
        String representativeName,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotBlank(message = "우편번호는 필수입니다.")
        String postalCode,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "상세 주소는 필수입니다.")
        String detailAddress
) {
}
