package com.ringdu.server.academy.schedule.repository;

import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademyClassStudentRepository extends JpaRepository<AcademyClassStudent, Long> {

    long countByAcademyClassIdAndStatus(Long academyClassId, ScheduleStatus status);

    List<AcademyClassStudent> findAllByAcademyClassIdAndStatus(Long academyClassId, ScheduleStatus status);

    List<AcademyClassStudent> findAllByStudentProfileIdAndStatus(Long studentProfileId, ScheduleStatus status);

    Optional<AcademyClassStudent> findByAcademyClassIdAndStudentProfileId(Long academyClassId, Long studentProfileId);

    List<AcademyClassStudent> findAllByAcademyClassIdInAndStatus(Collection<Long> academyClassIds, ScheduleStatus status);

    @Query("""
            select link
            from AcademyClassStudent link
            join AcademyClass academyClass on academyClass.id = link.academyClassId
            where link.studentProfileId = :studentProfileId
              and link.status = :status
              and academyClass.status = :status
            order by academyClass.name asc, academyClass.id asc
            """)
    List<AcademyClassStudent> findActiveLinksWithActiveClassByStudentProfileId(
            @Param("studentProfileId") Long studentProfileId,
            @Param("status") ScheduleStatus status
    );

    @Query("""
            select count(link) > 0
            from AcademyClassStudent link
            join AcademyClass academyClass on academyClass.id = link.academyClassId
            where link.studentProfileId = :studentProfileId
              and link.status = :status
              and academyClass.status = :status
              and academyClass.teacherUserId = :teacherUserId
            """)
    boolean existsActiveStudentForTeacher(
            @Param("teacherUserId") Long teacherUserId,
            @Param("studentProfileId") Long studentProfileId,
            @Param("status") ScheduleStatus status
    );
}
