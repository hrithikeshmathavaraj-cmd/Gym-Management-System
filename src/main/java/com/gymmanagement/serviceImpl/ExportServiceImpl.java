package com.gymmanagement.serviceImpl;

import com.gymmanagement.entity.LoginAudit;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.Payment;
import com.gymmanagement.entity.SalaryPayment;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.LoginAuditRepository;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.PaymentRepository;
import com.gymmanagement.repository.SalaryPaymentRepository;
import com.gymmanagement.service.ExportService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates downloadable Excel (.xlsx) and PDF exports of members, payments,
 * the login audit trail, and trainer salary payments.
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    public ByteArrayOutputStream exportMembersToExcel() {
        List<Member> members = memberRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Members");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Member Code", "Name", "Age", "Gender", "Phone", "Email", "Plan", "Join Date", "Expiry Date", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Member member : members) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(member.getMemberCode());
                row.createCell(1).setCellValue(member.getName());
                row.createCell(2).setCellValue(member.getAge());
                row.createCell(3).setCellValue(member.getGender());
                row.createCell(4).setCellValue(member.getPhone());
                row.createCell(5).setCellValue(member.getEmail());
                row.createCell(6).setCellValue(member.getMembershipPlan());
                row.createCell(7).setCellValue(member.getJoinDate() != null ? member.getJoinDate().format(DATE_FMT) : "");
                row.createCell(8).setCellValue(member.getExpiryDate() != null ? member.getExpiryDate().format(DATE_FMT) : "");
                row.createCell(9).setCellValue(member.getStatus().name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export members to Excel", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportMembersToPdf() {
        List<Member> members = memberRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Gym Members Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"Code", "Name", "Phone", "Email", "Plan", "Expiry", "Status"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new Color(15, 23, 42)); // Primary color #0F172A
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.HELVETICA, 9);
            for (Member member : members) {
                table.addCell(new Paragraph(member.getMemberCode(), cellFont));
                table.addCell(new Paragraph(member.getName(), cellFont));
                table.addCell(new Paragraph(member.getPhone() != null ? member.getPhone() : "-", cellFont));
                table.addCell(new Paragraph(member.getEmail() != null ? member.getEmail() : "-", cellFont));
                table.addCell(new Paragraph(member.getMembershipPlan(), cellFont));
                table.addCell(new Paragraph(member.getExpiryDate() != null ? member.getExpiryDate().format(DATE_FMT) : "-", cellFont));
                table.addCell(new Paragraph(member.getStatus().name(), cellFont));
            }

            document.add(table);
            document.close();
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export members to PDF", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportPaymentsToExcel() {
        List<Payment> payments = paymentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Transaction Id", "Member Id", "Amount", "Payment Date", "Method", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(payment.getTransactionId());
                row.createCell(1).setCellValue(payment.getMemberId());
                row.createCell(2).setCellValue(payment.getAmount());
                row.createCell(3).setCellValue(payment.getPaymentDate().toString());
                row.createCell(4).setCellValue(payment.getPaymentMethod());
                row.createCell(5).setCellValue(payment.getStatus().name());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export payments to Excel", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportLoginAuditToExcel() {
        List<LoginAudit> audits = loginAuditRepository.findAllByOrderByTimestampDesc();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Login Activity");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Login ID", "Name", "Role", "Action", "Detail", "IP Address", "Device", "Timestamp"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (LoginAudit audit : audits) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullToDash(audit.getUsername()));
                row.createCell(1).setCellValue(nullToDash(audit.getName()));
                row.createCell(2).setCellValue(nullToDash(audit.getRole()));
                row.createCell(3).setCellValue(audit.getAction().name());
                row.createCell(4).setCellValue(nullToDash(audit.getDetail()));
                row.createCell(5).setCellValue(nullToDash(audit.getIpAddress()));
                row.createCell(6).setCellValue(nullToDash(audit.getUserAgent()));
                row.createCell(7).setCellValue(audit.getTimestamp() != null ? audit.getTimestamp().format(DATETIME_FMT) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export login activity to Excel", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportLoginAuditToPdf() {
        List<LoginAudit> audits = loginAuditRepository.findAllByOrderByTimestampDesc();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Login Activity Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Login ID", "Name", "Role", "Action", "IP Address", "Timestamp"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new Color(15, 23, 42));
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.HELVETICA, 9);
            for (LoginAudit audit : audits) {
                table.addCell(new Paragraph(nullToDash(audit.getUsername()), cellFont));
                table.addCell(new Paragraph(nullToDash(audit.getName()), cellFont));
                table.addCell(new Paragraph(nullToDash(audit.getRole()), cellFont));
                table.addCell(new Paragraph(audit.getAction().name(), cellFont));
                table.addCell(new Paragraph(nullToDash(audit.getIpAddress()), cellFont));
                table.addCell(new Paragraph(audit.getTimestamp() != null ? audit.getTimestamp().format(DATETIME_FMT) : "-", cellFont));
            }

            document.add(table);
            document.close();
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export login activity to PDF", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportSalaryToExcel() {
        List<SalaryPayment> payments = salaryPaymentRepository.findAllByOrderByPaymentDateDesc();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trainer Salary");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Trainer", "Month", "Amount", "Method", "Status", "Payment Date", "Notes"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (SalaryPayment payment : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(payment.getTrainerName());
                row.createCell(1).setCellValue(payment.getMonth());
                row.createCell(2).setCellValue(payment.getAmount());
                row.createCell(3).setCellValue(nullToDash(payment.getMethod()));
                row.createCell(4).setCellValue(payment.getStatus().name());
                row.createCell(5).setCellValue(payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FMT) : "");
                row.createCell(6).setCellValue(nullToDash(payment.getNotes()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export trainer salary to Excel", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportSalaryToPdf() {
        List<SalaryPayment> payments = salaryPaymentRepository.findAllByOrderByPaymentDateDesc();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Trainer Salary Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Trainer", "Month", "Amount (₹)", "Method", "Status", "Payment Date"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setBackgroundColor(new Color(15, 23, 42));
                table.addCell(cell);
            }

            Font cellFont = new Font(Font.HELVETICA, 9);
            for (SalaryPayment payment : payments) {
                table.addCell(new Paragraph(payment.getTrainerName(), cellFont));
                table.addCell(new Paragraph(payment.getMonth(), cellFont));
                table.addCell(new Paragraph(String.format("%.2f", payment.getAmount()), cellFont));
                table.addCell(new Paragraph(nullToDash(payment.getMethod()), cellFont));
                table.addCell(new Paragraph(payment.getStatus().name(), cellFont));
                table.addCell(new Paragraph(payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FMT) : "-", cellFont));
            }

            document.add(table);
            document.close();
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to export trainer salary to PDF", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportSalarySlipPdf(String salaryPaymentId) {
        SalaryPayment payment = salaryPaymentRepository.findById(salaryPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary payment not found with id: " + salaryPaymentId));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Salary Payslip", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            Paragraph sub = new Paragraph("FitCore Gym Management", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            document.add(new Paragraph(" "));

            Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "Trainer Name", payment.getTrainerName(), labelFont, valueFont);
            addRow(table, "Salary Month", payment.getMonth(), labelFont, valueFont);
            addRow(table, "Amount Paid", String.format("Rs. %.2f", payment.getAmount()), labelFont, valueFont);
            addRow(table, "Payment Method", nullToDash(payment.getMethod()), labelFont, valueFont);
            addRow(table, "Payment Date", payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FMT) : "-", labelFont, valueFont);
            addRow(table, "Status", payment.getStatus().name(), labelFont, valueFont);
            addRow(table, "Notes", nullToDash(payment.getNotes()), labelFont, valueFont);
            document.add(table);

            document.add(new Paragraph(" "));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("This is a system-generated payslip.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate salary slip PDF", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportPaymentReceiptPdf(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        Member member = memberRepository.findById(payment.getMemberId()).orElse(null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Payment Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            Paragraph sub = new Paragraph("FitCore Gym Management", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);
            document.add(new Paragraph(" "));

            Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addRow(table, "Receipt No.", payment.getId(), labelFont, valueFont);
            addRow(table, "Member Name", member != null ? member.getName() : "-", labelFont, valueFont);
            addRow(table, "Member Code", member != null ? member.getMemberCode() : "-", labelFont, valueFont);
            addRow(table, "Amount Paid", String.format("Rs. %.2f", payment.getAmount()), labelFont, valueFont);
            addRow(table, "Payment Method", nullToDash(payment.getPaymentMethod()), labelFont, valueFont);
            addRow(table, "Payment Date", payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FMT) : "-", labelFont, valueFont);
            addRow(table, "Transaction ID", nullToDash(payment.getTransactionId()), labelFont, valueFont);
            addRow(table, "Status", payment.getStatus().name(), labelFont, valueFont);
            document.add(table);

            document.add(new Paragraph(" "));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph("This is a system-generated receipt.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payment receipt PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setPadding(6);
        PdfPCell valueCell = new PdfPCell(new Paragraph(value != null ? value : "-", valueFont));
        valueCell.setPadding(6);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
