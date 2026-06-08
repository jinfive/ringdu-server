package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import com.ringdu.server.consultation.entity.ConsultationTopic;
import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationRequestResponse(
        Long consultationRequestId,
        Long academyId,
        String academyName,
        Long studentProfileId,
        String studentName,
        Long teacherUserId,
        String teacherName,
        LocalDate requestedDate,
        LocalTime requestedStartTime,
        LocalTime requestedEndTime,
        ConsultationRequestType consultationType,
        ConsultationTopic topic,
        String topicLabel,
        String content,
        ConsultationRequestStatus status,
        String statusLabel,
        String academyMemo
) {

    public static ConsultationRequestResponse of(
            ConsultationRequest request,
            String academyName,
            String studentName,
            String teacherName
    ) {
        return new ConsultationRequestResponse(
                request.getId(),
                request.getAcademyId(),
                academyName,
                request.getStudentProfileId(),
                studentName,
                request.getTeacherUserId(),
                teacherName,
                request.getRequestedDate(),
                request.getRequestedStartTime(),
                request.getRequestedEndTime(),
                request.getConsultationType(),
                request.getTopic(),
                topicLabel(request.getTopic()),
                request.getContent(),
                request.getStatus(),
                statusLabel(request.getStatus()),
                request.getAcademyMemo()
        );
    }

    private static String topicLabel(ConsultationTopic topic) {
        return switch (topic) {
            case STUDY -> "학습 상담";
            case LIFE -> "생활 상담";
            case PROGRESS -> "진도 상담";
            case ETC -> "기타";
        };
    }

    private static String statusLabel(ConsultationRequestStatus status) {
        return switch (status) {
            case REQUESTED -> "요청됨";
            case APPROVED -> "승인됨";
            case REJECTED -> "거절됨";
            case COMPLETED -> "완료됨";
            case CANCELED -> "취소됨";
        };
    }
}
