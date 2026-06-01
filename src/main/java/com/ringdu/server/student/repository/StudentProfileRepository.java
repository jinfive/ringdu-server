package com.ringdu.server.student.repository;

import com.ringdu.server.student.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    List<StudentProfile> findAllByAcademyId(Long academyId);

    @Query("""
            select p
            from StudentProfile p
            where p.academyId = :academyId
              and lower(p.name) like lower(concat('%', :keyword, '%'))
            order by
              case when p.status = com.ringdu.server.student.entity.StudentStatus.ACTIVE then 0 else 1 end,
              p.name asc,
              p.id asc
            """)
    List<StudentProfile> searchByAcademyIdAndName(
            @Param("academyId") Long academyId,
            @Param("keyword") String keyword
    );

    long countByAcademyId(Long academyId);
    boolean existsByAcademyIdAndUserId(Long academyId, Long userId);
}
