package com.ringdu.server.parentstudent.repository;

import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParentStudentRelationRepository extends JpaRepository<ParentStudentRelation, Long> {

    boolean existsByParentIdAndStudentId(Long parentUserId, Long studentUserId);

    List<ParentStudentRelation> findAllByParentIdOrderByCreatedAtDesc(Long parentUserId);

    List<ParentStudentRelation> findAllByStudentIdOrderByCreatedAtDesc(Long studentUserId);

    @Query("""
            select relation
            from ParentStudentRelation relation
            join fetch relation.parent
            where relation.student.id = :studentUserId
              and relation.status = :status
            order by relation.createdAt desc
            """)
    List<ParentStudentRelation> findAllByStudentIdAndStatusWithParent(
            @Param("studentUserId") Long studentUserId,
            @Param("status") ParentStudentRelationStatus status
    );
}
