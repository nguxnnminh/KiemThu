package com.smartdental.controller;

import com.smartdental.dto.form.ComplexCaseCoefficientForm;
import com.smartdental.dto.form.ComplexCaseDecisionForm;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.Employee;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.ComplexCaseCoefficientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC4.3 - Nhap he so ca phuc tap trong thang.
 */
@Controller
@RequiredArgsConstructor
public class ComplexCaseCoefficientController {

    private final ComplexCaseCoefficientService complexCaseCoefficientService;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final UserRepository userRepository;

    @GetMapping("/payroll/complex-cases")
    public String list(@RequestParam(required = false) ApprovalStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<ComplexCaseCoefficient> coefficients = complexCaseCoefficientService.search(status, PageRequest.of(page, 20));
        model.addAttribute("coefficients", coefficients);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", ApprovalStatus.values());
        if (!model.containsAttribute("decisionForm")) {
            model.addAttribute("decisionForm", new ComplexCaseDecisionForm());
        }
        return "payroll/complex-cases";
    }

    @PostMapping("/payroll/complex-cases/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            complexCaseCoefficientService.approve(id);
            ra.addFlashAttribute("successMessage", "Duyệt hệ số ca phức tạp thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/complex-cases";
    }

    @PostMapping("/payroll/complex-cases/{id}/reject")
    public String reject(@PathVariable Long id, @ModelAttribute ComplexCaseDecisionForm form, RedirectAttributes ra) {
        try {
            complexCaseCoefficientService.reject(id, form);
            ra.addFlashAttribute("successMessage", "Từ chối hệ số ca phức tạp thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/complex-cases";
    }

    @GetMapping("/payroll/complex-cases/my")
    public String my(Authentication authentication, Model model) {
        Employee doctor = currentEmployee(authentication);
        if (doctor == null) {
            throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
        }
        model.addAttribute("coefficients", complexCaseCoefficientService.findByDoctorId(doctor.getId()));
        model.addAttribute("completedSessions",
                treatmentSessionRepository.findByDoctorIdOrderByExaminationDateDesc(doctor.getId()).stream()
                        .filter(s -> s.getStatus() == TreatmentSessionStatus.COMPLETED)
                        .toList());
        if (!model.containsAttribute("proposeForm")) {
            model.addAttribute("proposeForm", new ComplexCaseCoefficientForm());
        }
        return "payroll/my-complex-cases";
    }

    @PostMapping("/payroll/complex-cases/my")
    public String propose(@ModelAttribute ComplexCaseCoefficientForm form, Authentication authentication, RedirectAttributes ra) {
        try {
            Employee doctor = currentEmployee(authentication);
            if (doctor == null) {
                throw new BusinessException("Không tìm thấy thông tin bác sĩ.");
            }
            complexCaseCoefficientService.propose(form, doctor);
            ra.addFlashAttribute("successMessage", "Đề xuất hệ số ca phức tạp thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/complex-cases/my";
    }

    private Employee currentEmployee(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .map(com.smartdental.entity.User::getEmployee)
                .orElse(null);
    }
}
