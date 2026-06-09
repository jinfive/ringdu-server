package com.ringdu.server.billing.controller;

import com.ringdu.server.billing.dto.StudentBillingInquiryInvoiceResponse;
import com.ringdu.server.billing.dto.StudentBillingInquirySummaryResponse;
import com.ringdu.server.billing.service.StudentBillingInquiryService;
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
@RequestMapping("/api/student/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentBillingInquiryController {

    private final StudentBillingInquiryService billingInquiryService;

    @GetMapping("/invoices")
    public ApiResponse<List<StudentBillingInquiryInvoiceResponse>> getInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Integer year
    ) {
        return ApiResponse.success(billingInquiryService.getStudentInvoices(principal.userId(), year));
    }

    @GetMapping("/summary")
    public ApiResponse<StudentBillingInquirySummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ApiResponse.success(billingInquiryService.getStudentSummary(
                principal.userId(), year, month
        ));
    }
}
