package com.ringdu.server.academy.repository;

import com.ringdu.server.academy.entity.AcademyStudentInvitation;
import com.ringdu.server.academy.entity.AcademyStudentInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademyStudentInvitationRepository extends JpaRepository<AcademyStudentInvitation, Long> {
    List<AcademyStudentInvitation> findAllByAcademyId(Long academyId);
    List<AcademyStudentInvitation> findAllByReceiverUserId(Long receiverUserId);
    Optional<AcademyStudentInvitation> findByAcademyIdAndReceiverUserIdAndStatus(Long academyId, Long receiverUserId, AcademyStudentInvitationStatus status);
    boolean existsByAcademyIdAndReceiverUserIdAndStatus(Long academyId, Long receiverUserId, AcademyStudentInvitationStatus status);
}
