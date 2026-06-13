package com.smartdental.controller;

import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.PayrollExcelExportService;
import com.smartdental.service.PayrollReportService;
import com.smartdental.service.PayrollSlipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * UC4.4/4.6 - Man hinh tu xem cua bac si: phieu luong cua minh, bao cao luong nam cua minh.
 */
@Controller
@RequiredArgsConstructor
public class DoctorPayrollController {

    private final PayrollSlipService payrollSlipService;
    private final PayrollReportService payrollReportService;
    private final PayrollExcelExportService payrollExcelExportService;
    private final UserRepository userRepository;

    @GetMapping("/payroll/my-slips")
    public String mySlips(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        Employee doctor = currentEmployee(authentication);
        if (doctor == null) {
            throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
        }
        Page<PayrollSlip> slips = payrollSlipService.findByDoctor(doctor.getId(), PageRequest.of(page, 20));
        model.addAttribute("slips", slips);
        return "payroll/my-slips";
    }

    @GetMapping("/payroll/my-slips/{id}")
    public String mySlipDetail(@PathVariable Long id, Authentication authentication, Model model) {
        Employee doctor = currentEmployee(authentication);
        if (doctor == null) {
            throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
        }
        PayrollSlip slip = payrollSlipService.getById(id);
        if (!slip.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("Bạn không có quyền xem phiếu lương này.");
        }
        model.addAttribute("slip", slip);
        model.addAttribute("items", payrollSlipService.getItems(id));
        return "payroll/slip-detail";
    }

    @GetMapping("/payroll/my-yearly-report")
    public String myYearlyReport(@RequestParam(required = false) Integer year, Authentication authentication, Model model) {
        Employee doctor = currentEmployee(authentication);
        if (doctor == null) {
            throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
        }
        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<PayrollSlip> slips = payrollReportService.getDoctorYearlyReport(doctor.getId(), reportYear);
        model.addAttribute("slips", slips);
        model.addAttribute("year", reportYear);
        model.addAttribute("doctor", doctor);
        return "payroll/my-yearly-report";
    }

    @GetMapping("/payroll/my-yearly-report/export")
    public ResponseEntity<byte[]> exportMyYearlyReport(@RequestParam(required = false) Integer year, Authentication authentication) {
        Employee doctor = currentEmployee(authentication);
        if (doctor == null) {
            throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
        }
        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<PayrollSlip> slips = payrollReportService.getDoctorYearlyReport(doctor.getId(), reportYear);
        byte[] data = payrollExcelExportService.exportDoctorYearlyReport(doctor, reportYear, slips);
        String filename = String.format("bao-cao-luong-bac-si-%d.xlsx", reportYear);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    private Employee currentEmployee(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .map(com.smartdental.entity.User::getEmployee)
                .orElse(null);
    }
}
