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

    List<ConsultationAvailability> findAllByAcademyIdAndTeacherUserIdOrderByDayOfWeekAscStartTimeAscIdAsc(
            Long academyId,
            Long teacherUserId
    );

    List<ConsultationAvailability> findAllByAcademyIdOrderByTeacherUserIdAscDayOfWeekAscStartTimeAscIdAsc(Long academyId);

    List<ConsultationAvailability> findAllByTeacherUserIdOrderByAcademyIdAscDayOfWeekAscStartTimeAscIdAsc(Long teacherUserId);

    Optional<ConsultationAvailability> findByIdAndAcademyId(Long id, Long academyId);

    Optional<ConsultationAvailability> findByIdAndTeacherUserId(Long id, Long teacherUserId);

    @Query("""
            select count(availability) > 0
            from ConsultationAvailability availability
            where availability.academyId = :academyId
              and availability.teacherUserId = :teacherUserId
              and availability.dayOfWeek = :dayOfWeek
              and availability.status = com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus.ACTIVE
              and (:excludedId is null or availability.id <> :excludedId)
              and availability.startTime < :endTime
              and :startTime < availability.endTime
            """)
    boolean existsOverlappingActive(
            @Param("academyId") Long academyId,
            @Param("teacherUserId") Long teacherUserId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedId") Long excludedId
    );

    @Query("""
            select count(availability) > 0
            from ConsultationAvailability availability
            where availability.academyId = :academyId
              and availability.teacherUserId is null
              and availability.dayOfWeek = :dayOfWeek
              and availability.status = com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus.ACTIVE
              and (:excludedId is null or availability.id <> :excludedId)
              and availability.startTime < :endTime
              and :startTime < availability.endTime
            """)
    boolean existsOverlappingActiveAcademy(
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
              and availability.teacherUserId = :teacherUserId
              and availability.status = :status
              and (availability.consultationType = :consultationType
                   or availability.consultationType = com.ringdu.server.consultation.entity.ConsultationType.ALL)
            order by availability.dayOfWeek asc, availability.startTime asc, availability.id asc
            """)
    List<ConsultationAvailability> findActiveByAcademyIdAndTeacherUserIdAndConsultationType(
            @Param("academyId") Long academyId,
            @Param("teacherUserId") Long teacherUserId,
            @Param("consultationType") ConsultationType consultationType,
            @Param("status") ConsultationAvailabilityStatus status
    );

    @Query("""
            select availability
            from ConsultationAvailability availability
            where availability.academyId = :academyId
              and availability.teacherUserId is null
              and availability.status = :status
              and (availability.consultationType = :consultationType
                   or availability.consultationType = com.ringdu.server.consultation.entity.ConsultationType.ALL)
            order by availability.dayOfWeek asc, availability.startTime asc, availability.id asc
            """)
    List<ConsultationAvailability> findActiveAcademyByConsultationType(
            @Param("academyId") Long academyId,
            @Param("consultationType") ConsultationType consultationType,
            @Param("status") ConsultationAvailabilityStatus status
    );
}
