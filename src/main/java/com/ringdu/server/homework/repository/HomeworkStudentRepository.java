package com.ringdu.server.homework.repository;

import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HomeworkStudentRepository extends JpaRepository<HomeworkStudent, Long> {

    List<HomeworkStudent> findAllByHomeworkIdOrderByIdAsc(Long homeworkId);

    List<HomeworkStudent> findAllByHomeworkIdIn(Collection<Long> homeworkIds);

    @Query("""
            select hs
            from HomeworkStudent hs
            join Homework h on h.id = hs.homeworkId
            where hs.studentProfileId in :studentProfileIds
              and h.status = com.ringdu.server.homework.entity.HomeworkStatus.ACTIVE
              and (:status is null or hs.status = :status)
              and (:fromDate is null or h.dueDate >= :fromDate)
              and (:toDate is null or h.dueDate <= :toDate)
            order by h.dueDate desc, h.id desc, hs.id desc
            """)
    List<HomeworkStudent> findReadableHomeworks(
            @Param("studentProfileIds") Collection<Long> studentProfileIds,
            @Param("status") HomeworkStudentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
