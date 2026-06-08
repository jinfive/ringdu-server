package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

    List<ConsultationRequest> findAllByParentUserIdOrderByCreatedAtDescIdDesc(Long parentUserId);

    Optional<ConsultationRequest> findByIdAndAcademyId(Long id, Long academyId);

    boolean existsByAcademyIdAndStudentProfileIdAndRequestedDateAndRequestedStartTimeAndRequestedEndTimeAndStatus(
            Long academyId,
            Long studentProfileId,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime,
            ConsultationRequestStatus status
    );

    @Query("""
            select request
            from ConsultationRequest request
            where request.academyId = :academyId
              and (:status is null or request.status = :status)
              and (:from is null or request.requestedDate >= :from)
              and (:to is null or request.requestedDate <= :to)
              and (:type is null or request.consultationType = :type)
            order by request.createdAt desc, request.id desc
            """)
    List<ConsultationRequest> findAcademyRequests(
            @Param("academyId") Long academyId,
            @Param("status") ConsultationRequestStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("type") ConsultationRequestType type
    );
}
