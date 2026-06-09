package com.ringdu.server.billing.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@Entity
@Table(
        name = "student_billing_invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_billing_invoices_academy_student_month",
                columnNames = {"academy_id", "student_profile_id", "billing_month"}
        ),
        indexes = {
                @Index(name = "idx_student_billing_invoices_academy_student", columnList = "academy_id, student_profile_id"),
                @Index(name = "idx_student_billing_invoices_billing_month", columnList = "billing_month")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(constraints = "amount >= 0 AND paid_amount >= 0")
public class StudentBillingInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "paid_amount", nullable = false)
    private Long paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentBillingInvoiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public StudentBillingInvoice(
            Long academyId,
            Long studentProfileId,
            String billingMonth,
            LocalDate issuedDate,
            LocalDate dueDate,
            Long amount,
            String memo
    ) {
        this.academyId = academyId;
        this.studentProfileId = studentProfileId;
        this.billingMonth = billingMonth;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.paidAmount = 0L;
        this.status = StudentBillingInvoiceStatus.UNPAID;
        this.memo = memo;
    }

    public void updateAmount(Long amount, String memo) {
        validateActive();
        if (amount < paidAmount) {
            throw new BusinessException(ErrorCode.BILLING_INVOICE_AMOUNT_INVALID);
        }
        this.amount = amount;
        this.memo = memo;
        recalculateStatus();
    }

    public void addPayment(Long paymentAmount) {
        validateActive();
        if (paymentAmount <= 0 || paidAmount + paymentAmount > amount) {
            throw new BusinessException(ErrorCode.BILLING_PAYMENT_AMOUNT_INVALID);
        }
        paidAmount += paymentAmount;
        recalculateStatus();
    }

    public void cancel() {
        if (status == StudentBillingInvoiceStatus.CANCELED || status == StudentBillingInvoiceStatus.PAID) {
            throw new BusinessException(ErrorCode.BILLING_INVOICE_STATUS_INVALID);
        }
        status = StudentBillingInvoiceStatus.CANCELED;
    }

    public long getUnpaidAmount() {
        if (status == StudentBillingInvoiceStatus.CANCELED) {
            return 0L;
        }
        return Math.max(amount - paidAmount, 0L);
    }

    private void validateActive() {
        if (status == StudentBillingInvoiceStatus.CANCELED) {
            throw new BusinessException(ErrorCode.BILLING_INVOICE_STATUS_INVALID);
        }
    }

    private void recalculateStatus() {
        if (paidAmount == 0) {
            status = StudentBillingInvoiceStatus.UNPAID;
        } else if (paidAmount < amount) {
            status = StudentBillingInvoiceStatus.PARTIAL;
        } else {
            status = StudentBillingInvoiceStatus.PAID;
        }
    }
}
