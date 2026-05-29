package com.ringdu.server.academy.schedule.repository;

import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AcademyClassStudentRepository extends JpaRepository<AcademyClassStudent, Long> {

    long countByAcademyClassIdAndStatus(Long academyClassId, ScheduleStatus status);

    List<AcademyClassStudent> findAllByAcademyClassIdAndStatus(Long academyClassId, ScheduleStatus status);

    Optional<AcademyClassStudent> findByAcademyClassIdAndStudentProfileId(Long academyClassId, Long studentProfileId);

    List<AcademyClassStudent> findAllByAcademyClassIdInAndStatus(Collection<Long> academyClassIds, ScheduleStatus status);
}
