package com.ringdu.server.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceSessionCreateRequest(
        @NotNull LocalDate attendanceDate
) {
}
