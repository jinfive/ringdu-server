package com.ringdu.server.parentstudent.dto;

import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.student.entity.StudentProfile;
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
        List<StudentProfileOptionResponse> studentProfiles,
        LocalDateTime createdAt
) {

    public static ParentStudentRelationResponse from(ParentStudentRelation relation) {
        return from(relation, List.of());
    }

    public static ParentStudentRelationResponse from(ParentStudentRelation relation, List<StudentProfile> studentProfiles) {
        return new ParentStudentRelationResponse(
                relation.getId(),
                relation.getParent().getId(),
                relation.getParent().getName(),
                relation.getParent().getEmail(),
                relation.getStudent().getId(),
                relation.getStudent().getName(),
                relation.getStudent().getEmail(),
                relation.getStatus(),
                studentProfiles.stream().map(StudentProfileOptionResponse::from).toList(),
                relation.getCreatedAt()
        );
    }

    public record StudentProfileOptionResponse(
            Long studentProfileId,
            String studentName,
            Long academyId
    ) {

        public static StudentProfileOptionResponse from(StudentProfile profile) {
            return new StudentProfileOptionResponse(
                    profile.getId(),
                    profile.getName(),
                    profile.getAcademyId()
            );
        }
    }
}
