package com.ringdu.server.parentstudent.repository;

import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentStudentRelationRepository extends JpaRepository<ParentStudentRelation, Long> {

    boolean existsByParentIdAndStudentId(Long parentUserId, Long studentUserId);

    List<ParentStudentRelation> findAllByParentIdOrderByCreatedAtDesc(Long parentUserId);

    List<ParentStudentRelation> findAllByStudentIdOrderByCreatedAtDesc(Long studentUserId);
}
