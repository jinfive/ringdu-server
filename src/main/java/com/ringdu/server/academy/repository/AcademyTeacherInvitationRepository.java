package com.ringdu.server.academy.repository;

import com.ringdu.server.academy.entity.AcademyTeacherInvitation;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyTeacherInvitationRepository extends JpaRepository<AcademyTeacherInvitation, Long> {

    boolean existsByAcademyIdAndTeacherEmailAndStatus(
            Long academyId,
            String teacherEmail,
            AcademyTeacherInvitationStatus status
    );

    List<AcademyTeacherInvitation> findAllByAcademyIdOrderByCreatedAtDesc(Long academyId);

    List<AcademyTeacherInvitation> findAllByTeacherEmailOrderByCreatedAtDesc(String teacherEmail);
}
