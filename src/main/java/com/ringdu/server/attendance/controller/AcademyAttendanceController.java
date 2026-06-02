package com.ringdu.server.attendance.controller;

import com.ringdu.server.attendance.dto.AcademyAttendanceSessionSummaryResponse;
import com.ringdu.server.attendance.dto.AcademyStudentAttendanceRecordResponse;
import com.ringdu.server.attendance.dto.AttendanceSessionDetailResponse;
import com.ringdu.server.attendance.service.AttendanceService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/me")
@RequiredArgsConstructor
public class AcademyAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/classes/{classId}/attendance-sessions")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyAttendanceSessionSummaryResponse>> getClassAttendanceSessions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId
    ) {
        return ApiResponse.success(attendanceService.getAcademyClassAttendanceSessions(principal.userId(), classId));
    }

    @GetMapping("/attendance-sessions/{sessionId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AttendanceSessionDetailResponse> getAttendanceSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(attendanceService.getAcademyAttendanceSession(principal.userId(), sessionId));
    }

    @GetMapping("/students/{studentProfileId}/attendance-records")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyStudentAttendanceRecordResponse>> getStudentAttendanceRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId
    ) {
        return ApiResponse.success(attendanceService.getAcademyStudentAttendanceRecords(principal.userId(), studentProfileId));
    }
}
