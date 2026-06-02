package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.entity.Academy;

public record AttendanceAcademyOptionResponse(
        Long academyId,
        String academyName
) {

    public static AttendanceAcademyOptionResponse from(Academy academy) {
        return new AttendanceAcademyOptionResponse(academy.getId(), academy.getName());
    }
}
