package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.entity.AcademyMemberStatus;
import java.time.LocalDateTime;

public record AcademyTeacherResponse(
        Long teacherUserId,
        String name,
        String email,
        String phone,
        AcademyMemberStatus memberStatus,
        LocalDateTime connectedAt
) {

    public static AcademyTeacherResponse from(AcademyMember member) {
        return new AcademyTeacherResponse(
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getEmail(),
                member.getUser().getPhone(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}
