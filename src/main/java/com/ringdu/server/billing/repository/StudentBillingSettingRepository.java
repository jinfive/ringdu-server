package com.ringdu.server.billing.repository;

import com.ringdu.server.billing.entity.StudentBillingSetting;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentBillingSettingRepository extends JpaRepository<StudentBillingSetting, Long> {
    Optional<StudentBillingSetting> findByAcademyIdAndStudentProfileId(Long academyId, Long studentProfileId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select setting from StudentBillingSetting setting
            where setting.academyId = :academyId
              and setting.studentProfileId = :studentProfileId
            """)
    Optional<StudentBillingSetting> findForUpdate(
            @Param("academyId") Long academyId,
            @Param("studentProfileId") Long studentProfileId
    );
}
