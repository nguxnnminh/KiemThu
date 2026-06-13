package com.smartdental.controller;

import com.smartdental.dto.form.PatientForm;
import com.smartdental.entity.Patient;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.Gender;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.PatientManagementService;
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
 * UC2.6 - Quan ly ho so benh nhan.
 */
@Controller
@RequiredArgsConstructor
public class PatientManagementController {

    private static final int PAGE_SIZE = 10;

    private final PatientManagementService patientManagementService;

    @GetMapping("/schedule/patients")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) CommonStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<Patient> patients = patientManagementService.search(keyword, status, pageable);

        model.addAttribute("patients", patients);
        model.addAttribute("stats", patientManagementService.stats());
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", CommonStatus.values());
        model.addAttribute("genders", Gender.values());

        if (!model.containsAttribute("patientForm")) {
            model.addAttribute("patientForm", new PatientForm());
        }
        return "schedule/patients";
    }

    @PostMapping("/schedule/patients")
    public String create(@ModelAttribute PatientForm form, RedirectAttributes ra) {
        try {
            patientManagementService.create(form);
            ra.addFlashAttribute("successMessage", "Them benh nhan thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addPatientModal");
            ra.addFlashAttribute("patientForm", form);
        }
        return "redirect:/schedule/patients";
    }

    @PostMapping("/schedule/patients/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PatientForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            patientManagementService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat benh nhan thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editPatientModal-" + id);
            ra.addFlashAttribute("editPatientForm-" + id, form);
        }
        return "redirect:/schedule/patients";
    }

    @PostMapping("/schedule/patients/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            patientManagementService.deactivate(id);
            ra.addFlashAttribute("successMessage", "Da ngung hoat dong ho so benh nhan.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/patients";
    }

    @PostMapping("/schedule/patients/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            patientManagementService.activate(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai ho so benh nhan.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/patients";
    }
}
