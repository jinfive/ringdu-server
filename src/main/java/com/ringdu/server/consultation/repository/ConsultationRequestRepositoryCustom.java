package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import java.time.LocalDate;
import java.util.List;

public interface ConsultationRequestRepositoryCustom {

    List<ConsultationRequest> findAcademyRequests(
            Long academyId,
            ConsultationRequestStatus status,
            LocalDate from,
            LocalDate to,
            ConsultationRequestType type,
            Long studentProfileId
    );
}
