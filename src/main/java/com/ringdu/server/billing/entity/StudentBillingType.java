package com.ringdu.server.billing.entity;

public enum StudentBillingType {
    REGULAR("정규 수강료"),
    PREPAID("3개월 선납"),
    MAKEUP("보강비"),
    TEXTBOOK("교재비"),
    ETC("기타");

    private final String label;

    StudentBillingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
