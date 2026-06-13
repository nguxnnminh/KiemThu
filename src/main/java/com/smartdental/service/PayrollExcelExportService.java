package com.smartdental.service;

import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollSlip;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Xuat bao cao luong UC4.5/4.6/4.7 ra file Excel (Apache POI).
 */
@Service
public class PayrollExcelExportService {

    public byte[] exportMonthlyReport(int year, int month, List<PayrollSlip> slips) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bao cao luong thang");
            CellStyle headerStyle = headerStyle(workbook);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("BÁO CÁO LƯƠNG THÁNG " + month + "/" + year);

            Row header = sheet.createRow(2);
            String[] headers = {"STT", "Mã phiếu", "Bác sĩ", "Mã bác sĩ", "Trạng thái", "Tổng lương (VNĐ)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            BigDecimal total = BigDecimal.ZERO;
            for (PayrollSlip slip : slips) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowIdx - 2);
                row.createCell(1).setCellValue(slip.getSlipCode());
                row.createCell(2).setCellValue(slip.getDoctor().getFullName());
                row.createCell(3).setCellValue(slip.getDoctor().getEmployeeCode());
                row.createCell(4).setCellValue(slip.getStatus().getLabel());
                row.createCell(5).setCellValue(slip.getTotalSalary().doubleValue());
                if (slip.getStatus() != com.smartdental.enums.PayrollStatus.CANCELLED) {
                    total = total.add(slip.getTotalSalary());
                }
                rowIdx++;
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(4).setCellValue("Tổng cộng");
            totalRow.createCell(5).setCellValue(total.doubleValue());

            return toBytes(workbook);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] exportDoctorYearlyReport(Employee doctor, int year, List<PayrollSlip> slips) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bao cao luong nam");
            CellStyle headerStyle = headerStyle(workbook);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("BÁO CÁO LƯƠNG NĂM " + year + " - " + doctor.getFullName() + " (" + doctor.getEmployeeCode() + ")");

            Row header = sheet.createRow(2);
            String[] headers = {"Tháng", "Mã phiếu", "Trạng thái", "Tổng lương (VNĐ)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<Integer, PayrollSlip> byMonth = new TreeMap<>();
            for (PayrollSlip slip : slips) {
                byMonth.put(slip.getPayrollMonth(), slip);
            }

            int rowIdx = 3;
            BigDecimal total = BigDecimal.ZERO;
            for (int m = 1; m <= 12; m++) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue("Tháng " + m);
                PayrollSlip slip = byMonth.get(m);
                if (slip != null) {
                    row.createCell(1).setCellValue(slip.getSlipCode());
                    row.createCell(2).setCellValue(slip.getStatus().getLabel());
                    row.createCell(3).setCellValue(slip.getTotalSalary().doubleValue());
                    total = total.add(slip.getTotalSalary());
                } else {
                    row.createCell(1).setCellValue("-");
                    row.createCell(2).setCellValue("Chưa có");
                    row.createCell(3).setCellValue(0d);
                }
                rowIdx++;
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(2).setCellValue("Tổng cộng");
            totalRow.createCell(3).setCellValue(total.doubleValue());

            return toBytes(workbook);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] exportYearlyReport(int year, List<PayrollSlip> slips) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bao cao luong nam");
            CellStyle headerStyle = headerStyle(workbook);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("BÁO CÁO LƯƠNG TẤT CẢ BÁC SĨ NĂM " + year);

            Row header = sheet.createRow(2);
            String[] headers = {"STT", "Bác sĩ", "Mã bác sĩ", "Tháng", "Mã phiếu", "Tổng lương (VNĐ)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            int stt = 1;
            BigDecimal total = BigDecimal.ZERO;
            for (PayrollSlip slip : slips) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(slip.getDoctor().getFullName());
                row.createCell(2).setCellValue(slip.getDoctor().getEmployeeCode());
                row.createCell(3).setCellValue("Tháng " + slip.getPayrollMonth());
                row.createCell(4).setCellValue(slip.getSlipCode());
                row.createCell(5).setCellValue(slip.getTotalSalary().doubleValue());
                total = total.add(slip.getTotalSalary());
                rowIdx++;
            }

            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(4).setCellValue("Tổng cộng");
            totalRow.createCell(5).setCellValue(total.doubleValue());

            return toBytes(workbook);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
