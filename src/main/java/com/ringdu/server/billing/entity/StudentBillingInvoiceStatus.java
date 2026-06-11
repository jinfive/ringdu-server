package com.ringdu.server.billing.entity;

public enum StudentBillingInvoiceStatus {
    UNPAID("미납"),
    PARTIAL("부분수납"),
    PAID("완납"),
    CANCELED("취소");

    private final String label;

    StudentBillingInvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
