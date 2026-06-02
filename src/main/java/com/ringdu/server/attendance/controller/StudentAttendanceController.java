package com.ringdu.server.attendance.controller;

import com.ringdu.server.attendance.dto.AttendanceAcademyOptionResponse;
import com.ringdu.server.attendance.dto.StudentAttendanceRecordResponse;
import com.ringdu.server.attendance.service.AttendanceService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/academies")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<AttendanceAcademyOptionResponse>> getAcademies(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(attendanceService.getStudentAcademies(principal.userId()));
    }

    @GetMapping("/attendance-records")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<StudentAttendanceRecordResponse>> getAttendanceRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Long academyId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.success(attendanceService.getStudentAttendanceRecords(principal.userId(), academyId, year, month));
    }
}
