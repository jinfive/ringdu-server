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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent/children/{studentProfileId}/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
public class ParentStudentBillingController {

    private final StudentBillingInquiryService billingInquiryService;

    @GetMapping("/invoices")
    public ApiResponse<List<StudentBillingInquiryInvoiceResponse>> getInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @RequestParam(required = false) Integer year
    ) {
        return ApiResponse.success(billingInquiryService.getParentInvoices(
                principal.userId(), studentProfileId, year
        ));
    }

    @GetMapping("/summary")
    public ApiResponse<StudentBillingInquirySummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ApiResponse.success(billingInquiryService.getParentSummary(
                principal.userId(), studentProfileId, year, month
        ));
    }
}
