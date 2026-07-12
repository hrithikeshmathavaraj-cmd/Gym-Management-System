package com.gymmanagement.service;

import java.io.ByteArrayOutputStream;

public interface ExportService {
    ByteArrayOutputStream exportMembersToExcel();
    ByteArrayOutputStream exportMembersToPdf();
    ByteArrayOutputStream exportPaymentsToExcel();
    ByteArrayOutputStream exportLoginAuditToExcel();
    ByteArrayOutputStream exportLoginAuditToPdf();
    ByteArrayOutputStream exportSalaryToExcel();
    ByteArrayOutputStream exportSalaryToPdf();
    ByteArrayOutputStream exportSalarySlipPdf(String salaryPaymentId);
    ByteArrayOutputStream exportPaymentReceiptPdf(String paymentId);
}
