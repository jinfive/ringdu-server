package com.ringdu.server.billing.entity;

import com.ringdu.server.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "student_billing_payments",
        indexes = {
                @Index(name = "idx_student_billing_payments_invoice", columnList = "billing_invoice_id"),
                @Index(name = "idx_student_billing_payments_academy_student", columnList = "academy_id, student_profile_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentBillingPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_invoice_id", nullable = false)
    private Long billingInvoiceId;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public StudentBillingPayment(
            Long billingInvoiceId,
            Long academyId,
            Long studentProfileId,
            Long amount,
            LocalDate paymentDate,
            String memo
    ) {
        this.billingInvoiceId = billingInvoiceId;
        this.academyId = academyId;
        this.studentProfileId = studentProfileId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.memo = memo;
    }
}
