package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationMemo;
import com.ringdu.server.consultation.entity.ConsultationMemoStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationMemoRepository extends JpaRepository<ConsultationMemo, Long> {

    List<ConsultationMemo> findAllByAcademyIdAndStudentProfileIdAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
            Long academyId,
            Long studentProfileId,
            ConsultationMemoStatus status
    );

    List<ConsultationMemo> findAllByAcademyIdAndStudentProfileIdInAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
            Long academyId,
            Collection<Long> studentProfileIds,
            ConsultationMemoStatus status
    );

    List<ConsultationMemo> findAllByStudentProfileIdInAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
            Collection<Long> studentProfileIds,
            ConsultationMemoStatus status
    );

    Optional<ConsultationMemo> findByIdAndStatus(Long id, ConsultationMemoStatus status);
}
