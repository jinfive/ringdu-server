package com.ringdu.server.homework.repository;

import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeworkStudentRepository extends JpaRepository<HomeworkStudent, Long> {

    List<HomeworkStudent> findAllByHomeworkIdOrderByIdAsc(Long homeworkId);

    List<HomeworkStudent> findAllByStudentProfileIdInOrderByIdDesc(Collection<Long> studentProfileIds);

    long countByHomeworkIdAndStatus(Long homeworkId, HomeworkStudentStatus status);
}
