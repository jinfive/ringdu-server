package com.ringdu.server.parentstudent.repository;

import com.ringdu.server.parentstudent.entity.ParentStudentInvitation;
import com.ringdu.server.parentstudent.entity.ParentStudentInvitationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentStudentInvitationRepository extends JpaRepository<ParentStudentInvitation, Long> {

    boolean existsByRequesterUserIdAndReceiverEmailAndStatus(
            Long requesterUserId,
            String receiverEmail,
            ParentStudentInvitationStatus status
    );

    boolean existsByRequesterUserIdAndReceiverPhoneAndStatus(
            Long requesterUserId,
            String receiverPhone,
            ParentStudentInvitationStatus status
    );

    List<ParentStudentInvitation> findAllByReceiverEmailOrderByCreatedAtDesc(String receiverEmail);

    List<ParentStudentInvitation> findAllByReceiverPhoneOrderByCreatedAtDesc(String receiverPhone);

    List<ParentStudentInvitation> findAllByRequesterUserIdOrderByCreatedAtDesc(Long requesterUserId);
}
