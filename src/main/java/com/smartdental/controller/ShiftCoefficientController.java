package com.smartdental.controller;

import com.smartdental.dto.form.ShiftCoefficientForm;
import com.smartdental.enums.CommonStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.WorkShiftRepository;
import com.smartdental.service.ShiftCoefficientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC4.2 - Thiet lap he so ca lam viec.
 */
@Controller
@RequiredArgsConstructor
public class ShiftCoefficientController {

    private final ShiftCoefficientService shiftCoefficientService;
    private final WorkShiftRepository workShiftRepository;

    @GetMapping("/payroll/shift-coefficients")
    public String list(Model model) {
        model.addAttribute("coefficients", shiftCoefficientService.findAll());
        model.addAttribute("workShifts", workShiftRepository.findByStatus(CommonStatus.ACTIVE));
        if (!model.containsAttribute("coefficientForm")) {
            model.addAttribute("coefficientForm", new ShiftCoefficientForm());
        }
        return "payroll/shift-coefficients";
    }

    @PostMapping("/payroll/shift-coefficients")
    public String create(@ModelAttribute ShiftCoefficientForm form, RedirectAttributes ra) {
        try {
            shiftCoefficientService.create(form);
            ra.addFlashAttribute("successMessage", "Thiết lập hệ số ca thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/shift-coefficients";
    }

    @PostMapping("/payroll/shift-coefficients/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            shiftCoefficientService.cancel(id);
            ra.addFlashAttribute("successMessage", "Hủy hệ số ca thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/shift-coefficients";
    }
}
