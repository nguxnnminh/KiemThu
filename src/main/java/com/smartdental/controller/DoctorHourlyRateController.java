package com.smartdental.controller;

import com.smartdental.dto.form.HourlyRateForm;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.DoctorHourlyRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC4.1 - Thiet lap muc tien co ban cho mot gio lam viec.
 */
@Controller
@RequiredArgsConstructor
public class DoctorHourlyRateController {

    private final DoctorHourlyRateService hourlyRateService;

    @GetMapping("/payroll/hourly-rates")
    public String list(Model model) {
        model.addAttribute("rates", hourlyRateService.findAll());
        if (!model.containsAttribute("rateForm")) {
            model.addAttribute("rateForm", new HourlyRateForm());
        }
        return "payroll/hourly-rates";
    }

    @PostMapping("/payroll/hourly-rates")
    public String create(@ModelAttribute HourlyRateForm form, RedirectAttributes ra) {
        try {
            hourlyRateService.create(form);
            ra.addFlashAttribute("successMessage", "Thiết lập mức tiền/giờ thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/hourly-rates";
    }

    @PostMapping("/payroll/hourly-rates/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            hourlyRateService.cancel(id);
            ra.addFlashAttribute("successMessage", "Hủy mức tiền/giờ thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/hourly-rates";
    }
}
