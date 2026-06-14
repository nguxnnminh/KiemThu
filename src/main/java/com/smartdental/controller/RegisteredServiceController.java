package com.smartdental.controller;

import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.service.RegisteredServiceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC3.4 - Dang ky dich vu dieu tri cho phien kham.
 */
@Controller
@RequiredArgsConstructor
public class RegisteredServiceController {

    private final RegisteredServiceManagementService registeredServiceManagementService;
    private final TreatmentSessionRepository treatmentSessionRepository;

    @GetMapping("/clinical/services")
    public String services(@RequestParam(required = false) Long sessionId, Model model, RedirectAttributes ra) {
        if (sessionId == null) {
            ra.addFlashAttribute("errorMessage", "Vui long chon phien kham truoc khi dang ky dich vu dieu tri.");
            return "redirect:/clinical/examination";
        }

        TreatmentSession session = treatmentSessionRepository.findDetailById(sessionId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
        java.util.List<DentalService> dentalServices = registeredServiceManagementService.findActiveServices();

        model.addAttribute("examSession", session);
        model.addAttribute("registeredServices", registeredServiceManagementService.findBySession(sessionId));
        model.addAttribute("dentalServices", dentalServices);
        model.addAttribute("servicePricesByServiceId", registeredServiceManagementService.findCurrentPricesByService(dentalServices));

        if (!model.containsAttribute("registeredServiceForm")) {
            RegisteredServiceForm form = new RegisteredServiceForm();
            form.setSessionId(sessionId);
            model.addAttribute("registeredServiceForm", form);
        }
        return "clinical/registered-services";
    }

    @PostMapping("/clinical/services")
    public String register(@ModelAttribute RegisteredServiceForm form, RedirectAttributes ra) {
        try {
            registeredServiceManagementService.register(form);
            ra.addFlashAttribute("successMessage", "Dang ky dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/services?sessionId=" + form.getSessionId();
    }

    @PostMapping("/clinical/services/{id}")
    public String update(@PathVariable Long id, @ModelAttribute RegisteredServiceForm form,
                         @RequestParam Long sessionId, RedirectAttributes ra) {
        form.setSessionId(sessionId);
        try {
            registeredServiceManagementService.update(id, form);
            ra.addFlashAttribute("successMessage", "Cap nhat dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/services?sessionId=" + sessionId;
    }

    @PostMapping("/clinical/services/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam Long sessionId, RedirectAttributes ra) {
        try {
            registeredServiceManagementService.cancel(id);
            ra.addFlashAttribute("successMessage", "Da huy dich vu.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/services?sessionId=" + sessionId;
    }
}
