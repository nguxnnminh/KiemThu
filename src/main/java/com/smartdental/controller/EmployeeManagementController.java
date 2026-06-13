package com.smartdental.controller;

import com.smartdental.dto.form.EmployeeForm;
import com.smartdental.dto.form.LockUserForm;
import com.smartdental.entity.Employee;
import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.Gender;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.EmployeeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC1.2 - Quan ly nhan vien.
 */
@Controller
@RequiredArgsConstructor
public class EmployeeManagementController {

    private static final int PAGE_SIZE = 10;

    private final EmployeeManagementService employeeManagementService;

    @GetMapping("/system/employees")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) EmployeePosition position,
                        @RequestParam(required = false) EmployeeStatus status,
                        @RequestParam(required = false) DoctorDegree degree,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<Employee> employees = employeeManagementService.search(keyword, position, status, degree, pageable);

        model.addAttribute("employees", employees);
        model.addAttribute("stats", employeeManagementService.stats());
        model.addAttribute("keyword", keyword);
        model.addAttribute("positionFilter", position);
        model.addAttribute("statusFilter", status);
        model.addAttribute("degreeFilter", degree);
        model.addAttribute("positions", EmployeePosition.values());
        model.addAttribute("statuses", EmployeeStatus.values());
        model.addAttribute("degrees", DoctorDegree.values());
        model.addAttribute("genders", Gender.values());

        if (!model.containsAttribute("employeeForm")) {
            model.addAttribute("employeeForm", new EmployeeForm());
        }
        return "system/employees";
    }

    @PostMapping("/system/employees")
    public String create(@ModelAttribute EmployeeForm form, RedirectAttributes ra) {
        try {
            employeeManagementService.create(form);
            ra.addFlashAttribute("successMessage", "Them nhan vien thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addEmployeeModal");
            ra.addFlashAttribute("employeeForm", form);
        }
        return "redirect:/system/employees";
    }

    @PostMapping("/system/employees/{id}")
    public String update(@PathVariable Long id, @ModelAttribute EmployeeForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            employeeManagementService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat nhan vien thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editEmployeeModal-" + id);
            ra.addFlashAttribute("editEmployeeForm-" + id, form);
        }
        return "redirect:/system/employees";
    }

    @PostMapping("/system/employees/{id}/suspend")
    public String suspend(@PathVariable Long id, @ModelAttribute LockUserForm form, RedirectAttributes ra) {
        try {
            employeeManagementService.suspend(id, form);
            ra.addFlashAttribute("successMessage", "Da tam khoa nhan vien.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "suspendEmployeeModal-" + id);
        }
        return "redirect:/system/employees";
    }

    @PostMapping("/system/employees/{id}/reactivate")
    public String reactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            employeeManagementService.reactivate(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai nhan vien.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/system/employees";
    }

    @PostMapping("/system/employees/{id}/deactivate")
    public String deactivate(@PathVariable Long id, @ModelAttribute LockUserForm form, RedirectAttributes ra) {
        try {
            employeeManagementService.deactivate(id, form);
            ra.addFlashAttribute("successMessage", "Da cho nhan vien nghi viec.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "deactivateEmployeeModal-" + id);
        }
        return "redirect:/system/employees";
    }
}
