package com.ringdu.server.student.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AcademyStudentCreateRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        LocalDate birthDate,
        String school,
        String grade,
        String email,
        String phone,
        String guardianPhone,
        Long guardianParentUserId,
        String memo
) {
}
