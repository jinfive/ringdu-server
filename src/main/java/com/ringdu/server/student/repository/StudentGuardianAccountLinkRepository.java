package com.ringdu.server.student.repository;

import com.ringdu.server.student.entity.StudentGuardianAccountLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGuardianAccountLinkRepository extends JpaRepository<StudentGuardianAccountLink, Long> {

    Optional<StudentGuardianAccountLink> findByStudentProfileId(Long studentProfileId);

    @Query("""
            select link
            from StudentGuardianAccountLink link
            join fetch link.parent
            where link.studentProfileId = :studentProfileId
            """)
    Optional<StudentGuardianAccountLink> findByStudentProfileIdWithParent(
            @Param("studentProfileId") Long studentProfileId
    );

    boolean existsByStudentProfileIdAndParentId(Long studentProfileId, Long parentUserId);
}
