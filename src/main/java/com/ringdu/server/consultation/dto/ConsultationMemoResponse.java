package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationMemo;
import com.ringdu.server.consultation.entity.ConsultationMemoWriterRole;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationMemoResponse(
        Long consultationMemoId,
        Long academyId,
        String academyName,
        Long studentProfileId,
        String studentName,
        Long consultationRequestId,
        Long writerUserId,
        ConsultationMemoWriterRole writerRole,
        String writerName,
        String title,
        String content,
        String nextAction,
        LocalDate consultationDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConsultationMemoResponse of(
            ConsultationMemo memo,
            String academyName,
            String studentName,
            String writerName
    ) {
        return new ConsultationMemoResponse(
                memo.getId(),
                memo.getAcademyId(),
                academyName,
                memo.getStudentProfileId(),
                studentName,
                memo.getConsultationRequestId(),
                memo.getWriterUserId(),
                memo.getWriterRole(),
                writerName,
                memo.getTitle(),
                memo.getContent(),
                memo.getNextAction(),
                memo.getConsultationDate(),
                memo.getCreatedAt(),
                memo.getUpdatedAt()
        );
    }
}
