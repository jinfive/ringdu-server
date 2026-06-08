package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long>, ConsultationRequestRepositoryCustom {

    List<ConsultationRequest> findAllByParentUserIdOrderByCreatedAtDescIdDesc(Long parentUserId);

    Optional<ConsultationRequest> findByIdAndAcademyId(Long id, Long academyId);

    List<ConsultationRequest> findAllByStudentProfileIdInOrderByRequestedDateDescRequestedStartTimeDescIdDesc(
            Collection<Long> studentProfileIds
    );

    boolean existsByAcademyIdAndStudentProfileIdAndRequestedDateAndRequestedStartTimeAndRequestedEndTimeAndStatus(
            Long academyId,
            Long studentProfileId,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime,
            ConsultationRequestStatus status
    );

}
