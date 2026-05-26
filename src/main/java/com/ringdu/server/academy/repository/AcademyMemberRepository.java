package com.ringdu.server.academy.repository;

import com.ringdu.server.academy.entity.AcademyMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyMemberRepository extends JpaRepository<AcademyMember, Long> {

    boolean existsByAcademyIdAndUserId(Long academyId, Long userId);

    long countByAcademyId(Long academyId);

    List<AcademyMember> findAllByAcademyIdOrderByCreatedAtDesc(Long academyId);
}
