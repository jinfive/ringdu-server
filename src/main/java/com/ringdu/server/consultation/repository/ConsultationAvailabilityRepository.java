package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationAvailability;
import com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus;
import com.ringdu.server.consultation.entity.ConsultationType;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultationAvailabilityRepository extends JpaRepository<ConsultationAvailability, Long> {

    List<ConsultationAvailability> findAllByAcademyIdOrderByDayOfWeekAscStartTimeAscIdAsc(Long academyId);

    Optional<ConsultationAvailability> findByIdAndAcademyId(Long id, Long academyId);

    @Query("""
            select count(availability) > 0
            from ConsultationAvailability availability
            where availability.academyId = :academyId
              and availability.dayOfWeek = :dayOfWeek
              and availability.status = com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus.ACTIVE
              and (:excludedId is null or availability.id <> :excludedId)
              and availability.startTime < :endTime
              and :startTime < availability.endTime
            """)
    boolean existsOverlappingActive(
            @Param("academyId") Long academyId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedId") Long excludedId
    );

    @Query("""
            select availability
            from ConsultationAvailability availability
            where availability.academyId = :academyId
              and availability.status = :status
              and (availability.consultationType = :consultationType
                   or availability.consultationType = com.ringdu.server.consultation.entity.ConsultationType.ALL)
            order by availability.dayOfWeek asc, availability.startTime asc, availability.id asc
            """)
    List<ConsultationAvailability> findActiveByAcademyIdAndConsultationType(
            @Param("academyId") Long academyId,
            @Param("consultationType") ConsultationType consultationType,
            @Param("status") ConsultationAvailabilityStatus status
    );
}
