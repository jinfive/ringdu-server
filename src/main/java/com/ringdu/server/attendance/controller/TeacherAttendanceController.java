package com.ringdu.server.attendance.controller;

import com.ringdu.server.attendance.dto.AttendanceRecordSaveRequest;
import com.ringdu.server.attendance.dto.AttendanceSessionCreateRequest;
import com.ringdu.server.attendance.dto.AttendanceSessionDetailResponse;
import com.ringdu.server.attendance.dto.TeacherTodayClassResponse;
import com.ringdu.server.attendance.service.AttendanceService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/today-classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<TeacherTodayClassResponse>> getTodayClasses(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(attendanceService.getTeacherTodayClasses(principal.userId()));
    }

    @PostMapping("/classes/{classId}/attendance-sessions")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AttendanceSessionDetailResponse> createAttendanceSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody AttendanceSessionCreateRequest request
    ) {
        return ApiResponse.success(
                "출석부가 준비되었습니다.",
                attendanceService.createOrGetTeacherAttendanceSession(principal.userId(), classId, request.attendanceDate())
        );
    }

    @GetMapping("/attendance-sessions/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AttendanceSessionDetailResponse> getAttendanceSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long sessionId
    ) {
        return ApiResponse.success(attendanceService.getTeacherAttendanceSession(principal.userId(), sessionId));
    }

    @PutMapping("/attendance-sessions/{sessionId}/records")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AttendanceSessionDetailResponse> saveAttendanceRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody AttendanceRecordSaveRequest request
    ) {
        return ApiResponse.success(
                "출석이 저장되었습니다.",
                attendanceService.saveTeacherAttendanceRecords(principal.userId(), sessionId, request)
        );
    }
}
