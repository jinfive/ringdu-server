package com.ringdu.server.academy.schedule.repository;

import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AcademyClassRepository extends JpaRepository<AcademyClass, Long> {

    @Query("""
            select c
            from AcademyClass c
            where c.academyId = :academyId
              and (:dayOfWeek is null or :dayOfWeek member of c.dayOfWeeks or c.dayOfWeek = :dayOfWeek)
              and (:classroomId is null or c.classroomId = :classroomId)
              and (:status is null or c.status = :status)
            order by c.dayOfWeek asc, c.startTime asc, c.id asc
            """)
    List<AcademyClass> findSchedule(
            @Param("academyId") Long academyId,
            @Param("dayOfWeek") AcademyClassDayOfWeek dayOfWeek,
            @Param("classroomId") Long classroomId,
            @Param("status") ScheduleStatus status
    );

    Optional<AcademyClass> findByIdAndAcademyId(Long id, Long academyId);

    @Query("""
            select c
            from AcademyClass c
            where c.teacherUserId = :teacherUserId
              and (:dayOfWeek member of c.dayOfWeeks or c.dayOfWeek = :dayOfWeek)
              and c.status = :status
            order by c.startTime asc, c.id asc
            """)
    List<AcademyClass> findTeacherClassesByDay(
            @Param("teacherUserId") Long teacherUserId,
            @Param("dayOfWeek") AcademyClassDayOfWeek dayOfWeek,
            @Param("status") ScheduleStatus status
    );

    List<AcademyClass> findAllByTeacherUserIdAndStatusOrderByIdAsc(Long teacherUserId, ScheduleStatus status);

    @Query("""
            select count(c) > 0
            from AcademyClass c
            where c.academyId = :academyId
              and c.classroomId = :classroomId
              and (:dayOfWeek member of c.dayOfWeeks or c.dayOfWeek = :dayOfWeek)
              and c.status = com.ringdu.server.academy.schedule.entity.ScheduleStatus.ACTIVE
              and (:excludedClassId is null or c.id <> :excludedClassId)
              and c.startTime < :endTime
              and :startTime < c.endTime
            """)
    boolean existsOverlappingActiveClass(
            @Param("academyId") Long academyId,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") AcademyClassDayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedClassId") Long excludedClassId
    );
}
