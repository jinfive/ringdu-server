package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long>, ConsultationRequestRepositoryCustom {

    List<ConsultationRequest> findAllByParentUserIdOrderByCreatedAtDescIdDesc(Long parentUserId);

    Optional<ConsultationRequest> findByIdAndAcademyId(Long id, Long academyId);

    List<ConsultationRequest> findAllByStudentProfileIdInOrderByRequestedDateDescRequestedStartTimeDescIdDesc(
            Collection<Long> studentProfileIds
    );

    @Query("""
            select request
            from ConsultationRequest request
            where request.academyId = :academyId
              and request.teacherUserId = :teacherUserId
              and request.requestedDate between :startDate and :endDate
              and request.status in :statuses
            order by request.requestedDate asc, request.requestedStartTime asc, request.id asc
            """)
    List<ConsultationRequest> findOccupiedRequests(
            @Param("academyId") Long academyId,
            @Param("teacherUserId") Long teacherUserId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<ConsultationRequestStatus> statuses
    );

    @Query("""
            select count(request) > 0
            from ConsultationRequest request
            where request.academyId = :academyId
              and request.teacherUserId = :teacherUserId
              and request.requestedDate = :requestedDate
              and request.status in :statuses
              and request.requestedStartTime < :requestedEndTime
              and :requestedStartTime < request.requestedEndTime
            """)
    boolean existsOccupiedTime(
            @Param("academyId") Long academyId,
            @Param("teacherUserId") Long teacherUserId,
            @Param("requestedDate") LocalDate requestedDate,
            @Param("requestedStartTime") LocalTime requestedStartTime,
            @Param("requestedEndTime") LocalTime requestedEndTime,
            @Param("statuses") Collection<ConsultationRequestStatus> statuses
    );
}
