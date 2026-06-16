package com.ringdu.server.homework.repository;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findAllByClassIdAndStatusOrderByDueDateDescIdDesc(Long classId, HomeworkStatus status);

    Optional<Homework> findByIdAndClassIdAndStatus(Long id, Long classId, HomeworkStatus status);

    Optional<Homework> findByIdAndStatus(Long id, HomeworkStatus status);

    List<Homework> findAllByIdInAndStatus(Collection<Long> ids, HomeworkStatus status);
}
