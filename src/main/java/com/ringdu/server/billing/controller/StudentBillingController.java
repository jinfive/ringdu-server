package com.ringdu.server.billing.controller;

import com.ringdu.server.billing.dto.StudentBillingEnsureCurrentResponse;
import com.ringdu.server.billing.dto.StudentBillingInvoiceCreateRequest;
import com.ringdu.server.billing.dto.StudentBillingInvoiceResponse;
import com.ringdu.server.billing.dto.StudentBillingInvoiceUpdateRequest;
import com.ringdu.server.billing.dto.StudentBillingPaymentRequest;
import com.ringdu.server.billing.dto.StudentBillingSettingRequest;
import com.ringdu.server.billing.dto.StudentBillingSettingResponse;
import com.ringdu.server.billing.dto.StudentBillingSummaryResponse;
import com.ringdu.server.billing.service.StudentBillingService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/me/students/{studentProfileId}/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMY')")
public class StudentBillingController {

    private final StudentBillingService billingService;

    @GetMapping("/setting")
    public ApiResponse<StudentBillingSettingResponse> getSetting(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId
    ) {
        return ApiResponse.success(billingService.getSetting(principal.userId(), studentProfileId));
    }

    @PutMapping("/setting")
    public ApiResponse<StudentBillingSettingResponse> saveSetting(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @Valid @RequestBody StudentBillingSettingRequest request
    ) {
        return ApiResponse.success("수납 설정이 저장되었습니다.", billingService.saveSetting(
                principal.userId(),
                studentProfileId,
                request
        ));
    }

    @GetMapping("/summary")
    public ApiResponse<StudentBillingSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ApiResponse.success(billingService.getSummary(principal.userId(), studentProfileId, year, month));
    }

    @GetMapping("/invoices")
    public ApiResponse<List<StudentBillingInvoiceResponse>> getInvoices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.success(billingService.getInvoices(principal.userId(), studentProfileId, year, month));
    }

    @PostMapping("/invoices/ensure-current")
    public ApiResponse<StudentBillingEnsureCurrentResponse> ensureCurrentInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId
    ) {
        StudentBillingEnsureCurrentResponse response = billingService.ensureCurrentInvoice(
                principal.userId(),
                studentProfileId
        );
        return ApiResponse.success(response.message(), response);
    }

    @PostMapping("/invoices")
    public ApiResponse<StudentBillingInvoiceResponse> createInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @Valid @RequestBody StudentBillingInvoiceCreateRequest request
    ) {
        return ApiResponse.success("청구가 생성되었습니다.", billingService.createInvoice(
                principal.userId(), studentProfileId, request
        ));
    }

    @PutMapping("/invoices/{billingId}")
    public ApiResponse<StudentBillingInvoiceResponse> updateInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @PathVariable Long billingId,
            @Valid @RequestBody StudentBillingInvoiceUpdateRequest request
    ) {
        return ApiResponse.success("청구 금액이 수정되었습니다.", billingService.updateInvoice(
                principal.userId(), studentProfileId, billingId, request
        ));
    }

    @PostMapping("/invoices/{billingId}/payments")
    public ApiResponse<StudentBillingInvoiceResponse> addPayment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @PathVariable Long billingId,
            @Valid @RequestBody StudentBillingPaymentRequest request
    ) {
        return ApiResponse.success("수납이 반영되었습니다.", billingService.addPayment(
                principal.userId(), studentProfileId, billingId, request
        ));
    }

    @PostMapping("/invoices/{billingId}/cancel")
    public ApiResponse<StudentBillingInvoiceResponse> cancelInvoice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentProfileId,
            @PathVariable Long billingId
    ) {
        return ApiResponse.success("청구가 취소되었습니다.", billingService.cancelInvoice(
                principal.userId(), studentProfileId, billingId
        ));
    }
}
