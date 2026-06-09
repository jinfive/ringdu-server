package com.ringdu.server.billing.repository;

import com.ringdu.server.billing.entity.StudentBillingInvoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentBillingInvoiceRepository extends JpaRepository<StudentBillingInvoice, Long> {
    Optional<StudentBillingInvoice> findByAcademyIdAndStudentProfileIdAndBillingMonth(
            Long academyId,
            Long studentProfileId,
            String billingMonth
    );

    List<StudentBillingInvoice> findAllByAcademyIdAndStudentProfileIdOrderByBillingMonthDesc(
            Long academyId,
            Long studentProfileId
    );
}
