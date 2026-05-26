package com.ringdu.server.academy.dto;

import java.util.List;

public record AcademyDashboardResponse(
        int studentCount,
        int teacherCount,
        int unpaidInvoiceCount,
        int pendingConsultationCount,
        List<AcademyDashboardNotificationResponse> notifications
) {

    public static AcademyDashboardResponse empty() {
        return of(0);
    }

    public static AcademyDashboardResponse of(long teacherCount) {
        return new AcademyDashboardResponse(
                0,
                Math.toIntExact(teacherCount),
                0,
                0,
                List.of(new AcademyDashboardNotificationResponse(
                        "INFO",
                        "학생 등록이 필요합니다.",
                        "학생과 보호자 정보를 등록해 관리를 시작해 보세요.",
                        "/academy/students/new"
                ))
        );
    }
}
