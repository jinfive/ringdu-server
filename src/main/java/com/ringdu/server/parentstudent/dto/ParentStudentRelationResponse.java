package com.ringdu.server.parentstudent.dto;

import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ParentStudentRelationResponse(
        Long relationId,
        Long parentUserId,
        String parentName,
        String parentEmail,
        Long studentUserId,
        String studentName,
        String studentEmail,
        ParentStudentRelationStatus status,
        List<ParentStudentProfileResponse> studentProfiles,
        LocalDateTime createdAt
) {

    public static ParentStudentRelationResponse from(ParentStudentRelation relation) {
        return from(relation, List.of());
    }

    public static ParentStudentRelationResponse from(
            ParentStudentRelation relation,
            List<ParentStudentProfileResponse> studentProfiles
    ) {
        return new ParentStudentRelationResponse(
                relation.getId(),
                relation.getParent().getId(),
                relation.getParent().getName(),
                relation.getParent().getEmail(),
                relation.getStudent().getId(),
                relation.getStudent().getName(),
                relation.getStudent().getEmail(),
                relation.getStatus(),
                studentProfiles,
                relation.getCreatedAt()
        );
    }
}
