package com.ringdu.server.academy.schedule.repository;

import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademyClassroomRepository extends JpaRepository<AcademyClassroom, Long> {

    List<AcademyClassroom> findAllByAcademyIdAndStatusOrderByDisplayOrderAscIdAsc(Long academyId, ScheduleStatus status);

    Optional<AcademyClassroom> findByIdAndAcademyId(Long id, Long academyId);

    boolean existsByAcademyIdAndNameAndStatus(Long academyId, String name, ScheduleStatus status);

    int countByAcademyId(Long academyId);
}
