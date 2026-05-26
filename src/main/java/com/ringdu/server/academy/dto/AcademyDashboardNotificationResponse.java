package com.ringdu.server.academy.dto;

public record AcademyDashboardNotificationResponse(
        String type,
        String title,
        String message,
        String targetPath
) {
}
