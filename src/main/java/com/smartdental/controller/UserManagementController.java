package com.smartdental.controller;

import com.smartdental.dto.form.LockUserForm;
import com.smartdental.dto.form.ResetPasswordForm;
import com.smartdental.dto.form.UserForm;
import com.smartdental.entity.User;
import com.smartdental.enums.Role;
import com.smartdental.enums.UserStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * UC1.1 - Quan ly nguoi dung va phan quyen.
 */
@Controller
@RequiredArgsConstructor
public class UserManagementController {

    private static final int PAGE_SIZE = 10;

    private final UserManagementService userManagementService;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/system/users")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) UserStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<User> users = userManagementService.search(keyword, role, status, pageable);

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roleFilter", role);
        model.addAttribute("statusFilter", status);
        model.addAttribute("roles", Role.values());
        model.addAttribute("userStatuses", UserStatus.values());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());

        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        return "system/users";
    }

    @PostMapping("/system/users")
    public String create(@ModelAttribute UserForm form, RedirectAttributes ra) {
        try {
            userManagementService.create(form);
            ra.addFlashAttribute("successMessage", "Them tai khoan thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addUserModal");
            ra.addFlashAttribute("userForm", form);
        }
        return "redirect:/system/users";
    }

    @PostMapping("/system/users/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UserForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            userManagementService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat tai khoan thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editUserModal-" + id);
            ra.addFlashAttribute("editUserForm-" + id, form);
        }
        return "redirect:/system/users";
    }

    @PostMapping("/system/users/{id}/lock")
    public String lock(@PathVariable Long id, @ModelAttribute LockUserForm form,
                        Authentication authentication, RedirectAttributes ra) {
        try {
            userManagementService.lock(id, form, authentication.getName());
            ra.addFlashAttribute("successMessage", "Da khoa tai khoan.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "lockUserModal-" + id);
        }
        return "redirect:/system/users";
    }

    @PostMapping("/system/users/{id}/unlock")
    public String unlock(@PathVariable Long id, @ModelAttribute LockUserForm form,
                          Authentication authentication, RedirectAttributes ra) {
        try {
            userManagementService.unlock(id, form, authentication.getName());
            ra.addFlashAttribute("successMessage", "Da mo khoa tai khoan.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "lockUserModal-" + id);
        }
        return "redirect:/system/users";
    }

    @PostMapping("/system/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @ModelAttribute ResetPasswordForm form, RedirectAttributes ra) {
        try {
            String newPassword = userManagementService.resetPassword(id, form);
            ra.addFlashAttribute("successMessage", "Dat lai mat khau thanh cong. Mat khau moi: " + newPassword);
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "resetPasswordModal-" + id);
        }
        return "redirect:/system/users";
    }
}
