package com.ringdu.server.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcademyUpdateRequest(
        @NotBlank(message = "학원명은 필수입니다.")
        @Size(max = 100, message = "학원명은 100자 이하여야 합니다.")
        String name,

        @NotBlank(message = "대표자명은 필수입니다.")
        @Size(max = 50, message = "대표자명은 50자 이하여야 합니다.")
        String representativeName,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
        String phone,

        @Size(max = 20, message = "우편번호는 20자 이하여야 합니다.")
        String postalCode,

        @Size(max = 255, message = "기본 주소는 255자 이하여야 합니다.")
        String address,

        @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
        String detailAddress
) {
}
