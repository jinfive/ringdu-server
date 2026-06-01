package com.ringdu.server.attendance.dto;

import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AttendanceRecordSaveRequest(
        @NotEmpty List<@Valid RecordItem> records
) {

    public record RecordItem(
            @NotNull Long studentProfileId,
            @NotNull AttendanceRecordStatus status,
            @Size(max = 1000) String memo
    ) {
    }
}
