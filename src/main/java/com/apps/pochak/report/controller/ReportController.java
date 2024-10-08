package com.apps.pochak.report.controller;

import com.apps.pochak.auth.Auth;
import com.apps.pochak.auth.MemberOnly;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.api_payload.ApiResponse;
import com.apps.pochak.report.dto.request.ReportUploadRequest;
import com.apps.pochak.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.apps.pochak.global.api_payload.code.status.SuccessStatus.SUCCESS_UPLOAD_REPORT;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("")
    @MemberOnly
    public ApiResponse<Void> uploadReport(
            @Auth final Accessor accessor,
            @RequestBody @Valid final ReportUploadRequest request
    ) {
        reportService.saveReport(accessor, request);
        return ApiResponse.of(SUCCESS_UPLOAD_REPORT);
    }
}
