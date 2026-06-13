package com.smartdental.controller;

import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.service.PayrollExcelExportService;
import com.smartdental.service.PayrollReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * UC4.5/4.6/4.7 - Bao cao luong bac si theo thang/nam.
 */
@Controller
@RequiredArgsConstructor
public class PayrollReportController {

    private final PayrollReportService payrollReportService;
    private final PayrollExcelExportService payrollExcelExportService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/payroll/reports/monthly")
    public String monthlyReport(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 Model model) {
        LocalDate now = LocalDate.now();
        int reportYear = year != null ? year : now.getYear();
        int reportMonth = month != null ? month : now.getMonthValue();
        List<PayrollSlip> slips = payrollReportService.getMonthlyReport(reportYear, reportMonth);
        model.addAttribute("slips", slips);
        model.addAttribute("year", reportYear);
        model.addAttribute("month", reportMonth);
        return "payroll/monthly-report";
    }

    @GetMapping("/payroll/reports/monthly/export")
    public ResponseEntity<byte[]> exportMonthlyReport(@RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int reportYear = year != null ? year : now.getYear();
        int reportMonth = month != null ? month : now.getMonthValue();
        List<PayrollSlip> slips = payrollReportService.getMonthlyReport(reportYear, reportMonth);
        byte[] data = payrollExcelExportService.exportMonthlyReport(reportYear, reportMonth, slips);
        String filename = String.format("bao-cao-luong-thang-%d-%02d.xlsx", reportYear, reportMonth);
        return excelResponse(data, filename);
    }

    @GetMapping("/payroll/reports/doctor-yearly")
    public String doctorYearlyReport(@RequestParam(required = false) Long doctorId,
                                      @RequestParam(required = false) Integer year,
                                      Model model) {
        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<Employee> doctors = employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE);
        model.addAttribute("doctors", doctors);
        model.addAttribute("year", reportYear);
        model.addAttribute("doctorId", doctorId);
        if (doctorId != null) {
            Employee doctor = employeeRepository.findById(doctorId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy bác sĩ."));
            model.addAttribute("doctor", doctor);
            model.addAttribute("slips", payrollReportService.getDoctorYearlyReport(doctorId, reportYear));
        }
        return "payroll/doctor-yearly-report";
    }

    @GetMapping("/payroll/reports/doctor-yearly/export")
    public ResponseEntity<byte[]> exportDoctorYearlyReport(@RequestParam Long doctorId,
                                                            @RequestParam Integer year) {
        Employee doctor = employeeRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bác sĩ."));
        List<PayrollSlip> slips = payrollReportService.getDoctorYearlyReport(doctorId, year);
        byte[] data = payrollExcelExportService.exportDoctorYearlyReport(doctor, year, slips);
        String filename = String.format("bao-cao-luong-bac-si-%d.xlsx", year);
        return excelResponse(data, filename);
    }

    @GetMapping("/payroll/reports/yearly")
    public String yearlyReport(@RequestParam(required = false) Integer year, Model model) {
        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<PayrollSlip> slips = payrollReportService.getYearlyReport(reportYear);
        model.addAttribute("slips", slips);
        model.addAttribute("year", reportYear);
        return "payroll/yearly-report";
    }

    @GetMapping("/payroll/reports/yearly/export")
    public ResponseEntity<byte[]> exportYearlyReport(@RequestParam(required = false) Integer year) {
        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<PayrollSlip> slips = payrollReportService.getYearlyReport(reportYear);
        byte[] data = payrollExcelExportService.exportYearlyReport(reportYear, slips);
        String filename = String.format("bao-cao-luong-nam-%d.xlsx", reportYear);
        return excelResponse(data, filename);
    }

    private ResponseEntity<byte[]> excelResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}
