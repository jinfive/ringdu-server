package com.ringdu.server.attendance.controller;

import com.ringdu.server.attendance.dto.AttendanceAcademyOptionResponse;
import com.ringdu.server.attendance.dto.ParentChildAttendanceRecordResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent/children/{studentProfileId}")
@RequiredArgsConstructor
public class ParentAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/academies")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<AttendanceAcademyOptionResponse>> getChildAcademies(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId
    ) {
        return ApiResponse.success(attendanceService.getParentChildAcademies(principal.userId(), studentProfileId));
    }

    @GetMapping("/attendance-records")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ParentChildAttendanceRecordResponse>> getChildAttendanceRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @RequestParam(required = false) Long academyId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.success(attendanceService.getParentChildAttendanceRecords(
                principal.userId(),
                studentProfileId,
                academyId,
                year,
                month
        ));
    }
}
