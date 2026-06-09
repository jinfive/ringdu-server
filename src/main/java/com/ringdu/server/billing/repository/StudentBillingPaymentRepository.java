package com.ringdu.server.billing.repository;

import com.ringdu.server.billing.entity.StudentBillingPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentBillingPaymentRepository extends JpaRepository<StudentBillingPayment, Long> {
}
