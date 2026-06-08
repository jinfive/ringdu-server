package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationMemo;
import com.ringdu.server.consultation.entity.ConsultationMemoWriterRole;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConsultationMemoResponse(
        Long consultationMemoId,
        Long academyId,
        Long studentProfileId,
        String studentName,
        Long consultationRequestId,
        ConsultationMemoWriterRole writerRole,
        String writerName,
        String title,
        String content,
        String nextAction,
        LocalDate consultationDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConsultationMemoResponse of(ConsultationMemo memo, String studentName, String writerName) {
        return new ConsultationMemoResponse(
                memo.getId(),
                memo.getAcademyId(),
                memo.getStudentProfileId(),
                studentName,
                memo.getConsultationRequestId(),
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
