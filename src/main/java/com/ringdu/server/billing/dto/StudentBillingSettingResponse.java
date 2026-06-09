package com.ringdu.server.billing.dto;

import com.ringdu.server.billing.entity.StudentBillingSetting;

public record StudentBillingSettingResponse(
        Long studentProfileId,
        Long monthlyTuition,
        Integer dueDay,
        String memo,
        boolean configured
) {
    public static StudentBillingSettingResponse empty(Long studentProfileId) {
        return new StudentBillingSettingResponse(studentProfileId, 0L, 1, null, false);
    }

    public static StudentBillingSettingResponse from(StudentBillingSetting setting) {
        return new StudentBillingSettingResponse(
                setting.getStudentProfileId(),
                setting.getMonthlyTuition(),
                setting.getDueDay(),
                setting.getMemo(),
                true
        );
    }
}
