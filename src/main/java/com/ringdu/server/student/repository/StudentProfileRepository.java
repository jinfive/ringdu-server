package com.ringdu.server.student.repository;

import com.ringdu.server.student.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    List<StudentProfile> findAllByAcademyId(Long academyId);
    long countByAcademyId(Long academyId);
    boolean existsByAcademyIdAndUserId(Long academyId, Long userId);
}
