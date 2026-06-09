package com.ringdu.server.billing.repository;

import com.ringdu.server.billing.entity.StudentBillingSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentBillingSettingRepository extends JpaRepository<StudentBillingSetting, Long> {
    Optional<StudentBillingSetting> findByAcademyIdAndStudentProfileId(Long academyId, Long studentProfileId);
}
