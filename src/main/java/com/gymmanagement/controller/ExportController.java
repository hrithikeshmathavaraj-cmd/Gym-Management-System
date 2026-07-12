package com.gymmanagement.controller;

import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.PaymentRepository;
import com.gymmanagement.security.UserPrincipal;
import com.gymmanagement.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Download endpoints for exporting reports as Excel (.xlsx) or PDF.
 */
@RestController
@RequestMapping("/api/reports/export")
@RequiredArgsConstructor
@Tag(name = "Reports - Export", description = "Excel and PDF export endpoints")
public class ExportController {

    private final ExportService exportService;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/members/excel")
    @Operation(summary = "Download members list as an Excel file")
    public ResponseEntity<byte[]> exportMembersExcel() {
        byte[] data = exportService.exportMembersToExcel().toByteArray();
        return buildDownload(data, "members-report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/members/pdf")
    @Operation(summary = "Download members list as a PDF file")
    public ResponseEntity<byte[]> exportMembersPdf() {
        byte[] data = exportService.exportMembersToPdf().toByteArray();
        return buildDownload(data, "members-report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/payments/excel")
    @Operation(summary = "Download payments list as an Excel file")
    public ResponseEntity<byte[]> exportPaymentsExcel() {
        byte[] data = exportService.exportPaymentsToExcel().toByteArray();
        return buildDownload(data, "payments-report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/login-activity/excel")
    @Operation(summary = "Download the login/logout activity log as an Excel file")
    public ResponseEntity<byte[]> exportLoginActivityExcel() {
        byte[] data = exportService.exportLoginAuditToExcel().toByteArray();
        return buildDownload(data, "login-activity.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/login-activity/pdf")
    @Operation(summary = "Download the login/logout activity log as a PDF file")
    public ResponseEntity<byte[]> exportLoginActivityPdf() {
        byte[] data = exportService.exportLoginAuditToPdf().toByteArray();
        return buildDownload(data, "login-activity.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/salary/excel")
    @Operation(summary = "Download the trainer salary log as an Excel file")
    public ResponseEntity<byte[]> exportSalaryExcel() {
        byte[] data = exportService.exportSalaryToExcel().toByteArray();
        return buildDownload(data, "trainer-salary.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/salary/pdf")
    @Operation(summary = "Download the trainer salary log as a PDF file")
    public ResponseEntity<byte[]> exportSalaryPdf() {
        byte[] data = exportService.exportSalaryToPdf().toByteArray();
        return buildDownload(data, "trainer-salary.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/salary/{salaryPaymentId}/slip")
    @Operation(summary = "Download an individual salary payslip as a PDF file")
    public ResponseEntity<byte[]> exportSalarySlip(@PathVariable String salaryPaymentId) {
        byte[] data = exportService.exportSalarySlipPdf(salaryPaymentId).toByteArray();
        return buildDownload(data, "salary-slip-" + salaryPaymentId + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/payments/{paymentId}/receipt")
    @Operation(summary = "Download an individual payment receipt as a PDF file")
    public ResponseEntity<byte[]> exportPaymentReceipt(@PathVariable String paymentId, @AuthenticationPrincipal UserPrincipal principal) {
        boolean isMember = principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER"));
        if (isMember) {
            var payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
            var member = memberRepository.findByMemberCode(principal.getUsername()).orElse(null);
            if (member == null || !member.getId().equals(payment.getMemberId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        byte[] data = exportService.exportPaymentReceiptPdf(paymentId).toByteArray();
        return buildDownload(data, "payment-receipt-" + paymentId + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> buildDownload(byte[] data, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
