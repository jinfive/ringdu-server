package com.ringdu.server.homework.controller;

import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.homework.dto.HomeworkInquiryResponse;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.homework.service.HomeworkService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/homeworks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentHomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping
    public ApiResponse<List<HomeworkInquiryResponse>> getHomeworks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) HomeworkStudentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(homeworkService.getStudentHomeworks(principal.userId(), status, from, to));
    }
}
