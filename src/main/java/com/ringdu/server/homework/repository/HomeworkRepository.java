package com.ringdu.server.homework.repository;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findAllByAcademyClassIdAndStatusOrderByDueDateDescIdDesc(Long academyClassId, HomeworkStatus status);

    Optional<Homework> findByIdAndAcademyClassIdAndStatus(Long id, Long academyClassId, HomeworkStatus status);

    List<Homework> findAllByIdInAndStatus(Collection<Long> ids, HomeworkStatus status);
}
