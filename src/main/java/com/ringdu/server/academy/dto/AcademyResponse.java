package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyStatus;
import java.time.LocalDateTime;

public record AcademyResponse(
        Long academyId,
        String name,
        String representativeName,
        String phone,
        String postalCode,
        String address,
        String detailAddress,
        AcademyStatus status,
        LocalDateTime createdAt
) {

    public static AcademyResponse from(Academy academy) {
        return new AcademyResponse(
                academy.getId(),
                academy.getName(),
                academy.getRepresentativeName(),
                academy.getPhone(),
                academy.getPostalCode(),
                academy.getAddress(),
                academy.getDetailAddress(),
                academy.getStatus(),
                academy.getCreatedAt()
        );
    }
}
