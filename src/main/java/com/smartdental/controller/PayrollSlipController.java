package com.smartdental.controller;

import com.smartdental.dto.form.PayrollSlipCreateForm;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.service.PayrollSlipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC4.4 - Lap phieu luong bac si theo thang.
 */
@Controller
@RequiredArgsConstructor
public class PayrollSlipController {

    private final PayrollSlipService payrollSlipService;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/payroll/slips")
    public String list(@RequestParam(required = false) Integer year,
                        @RequestParam(required = false) Integer month,
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<PayrollSlip> slips = payrollSlipService.search(year, month, doctorId, PageRequest.of(page, 20));
        model.addAttribute("slips", slips);
        model.addAttribute("yearFilter", year);
        model.addAttribute("monthFilter", month);
        model.addAttribute("doctorFilter", doctorId);
        model.addAttribute("doctors", employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE));
        if (!model.containsAttribute("createForm")) {
            model.addAttribute("createForm", new PayrollSlipCreateForm());
        }
        return "payroll/slips";
    }

    @PostMapping("/payroll/slips/create")
    public String create(@ModelAttribute PayrollSlipCreateForm form, RedirectAttributes ra) {
        try {
            PayrollSlip slip = payrollSlipService.create(form);
            ra.addFlashAttribute("successMessage", "Lập phiếu lương thành công.");
            return "redirect:/payroll/slips/" + slip.getId();
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/payroll/slips";
        }
    }

    @GetMapping("/payroll/slips/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PayrollSlip slip = payrollSlipService.getById(id);
        model.addAttribute("slip", slip);
        model.addAttribute("items", payrollSlipService.getItems(id));
        return "payroll/slip-detail";
    }

    @PostMapping("/payroll/slips/{id}/recalculate")
    public String recalculate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            payrollSlipService.recalculate(id);
            ra.addFlashAttribute("successMessage", "Tính lại phiếu lương thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/slips/" + id;
    }

    @PostMapping("/payroll/slips/{id}/submit")
    public String submit(@PathVariable Long id, RedirectAttributes ra) {
        try {
            payrollSlipService.submit(id);
            ra.addFlashAttribute("successMessage", "Gửi duyệt phiếu lương thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/slips/" + id;
    }

    @PostMapping("/payroll/slips/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            payrollSlipService.approve(id);
            ra.addFlashAttribute("successMessage", "Duyệt phiếu lương thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/slips/" + id;
    }

    @PostMapping("/payroll/slips/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            payrollSlipService.cancel(id);
            ra.addFlashAttribute("successMessage", "Hủy phiếu lương thành công.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/payroll/slips/" + id;
    }
}
