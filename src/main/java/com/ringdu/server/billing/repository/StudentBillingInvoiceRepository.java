package com.ringdu.server.billing.repository;

import com.ringdu.server.billing.entity.StudentBillingInvoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentBillingInvoiceRepository extends JpaRepository<StudentBillingInvoice, Long> {
    @Query("""
            select invoice from StudentBillingInvoice invoice
            where invoice.academyId = :academyId
              and invoice.studentProfileId = :studentProfileId
              and invoice.billingMonth = :billingMonth
              and (invoice.billingType = com.ringdu.server.billing.entity.StudentBillingType.REGULAR
                   or invoice.billingType is null)
            order by invoice.id asc
            """)
    List<StudentBillingInvoice> findRegularInvoices(
            @Param("academyId") Long academyId,
            @Param("studentProfileId") Long studentProfileId,
            @Param("billingMonth") String billingMonth
    );

    List<StudentBillingInvoice> findAllByAcademyIdAndStudentProfileIdOrderByBillingMonthDesc(
            Long academyId,
            Long studentProfileId
    );

    List<StudentBillingInvoice> findAllByAcademyIdAndStudentProfileIdAndBillingMonthOrderByIdAsc(
            Long academyId,
            Long studentProfileId,
            String billingMonth
    );

    List<StudentBillingInvoice> findAllByStudentProfileIdInOrderByBillingMonthDescIdDesc(
            List<Long> studentProfileIds
    );
}
