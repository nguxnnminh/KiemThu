package com.smartdental.controller;

import com.smartdental.dto.form.ToothUpdateForm;
import com.smartdental.entity.Patient;
import com.smartdental.enums.ToothStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.PatientRepository;
import com.smartdental.service.DentalChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC3.3 - So do rang FDI 32.
 */
@Controller
@RequiredArgsConstructor
public class DentalChartController {

    private final DentalChartService dentalChartService;
    private final PatientRepository patientRepository;

    @GetMapping("/clinical/tooth-chart")
    public String chart(@RequestParam(required = false) Long patientId,
                         @RequestParam(required = false) Long sessionId,
                         @RequestParam(required = false) Integer toothNumber,
                         @RequestParam(required = false) String keyword,
                         Model model) {
        if (patientId == null) {
            if (keyword != null && !keyword.isBlank()) {
                model.addAttribute("results", patientRepository.search(keyword, null, PageRequest.of(0, 20)).getContent());
            }
            model.addAttribute("keyword", keyword);
            return "clinical/tooth-chart-lookup";
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            model.addAttribute("notFound", true);
            model.addAttribute("keyword", keyword);
            return "clinical/tooth-chart-lookup";
        }

        model.addAttribute("patient", patient);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("chart", dentalChartService.getChart(patientId));
        model.addAttribute("toothStatuses", ToothStatus.values());

        if (toothNumber != null) {
            model.addAttribute("selectedTooth", toothNumber);
            model.addAttribute("history", dentalChartService.getHistory(patientId, toothNumber));
        }

        if (!model.containsAttribute("toothForm")) {
            ToothUpdateForm form = new ToothUpdateForm();
            form.setPatientId(patientId);
            form.setSessionId(sessionId);
            form.setToothNumber(toothNumber);
            form.setReturnTo("tooth-chart");
            model.addAttribute("toothForm", form);
        }
        return "clinical/tooth-chart";
    }

    @PostMapping("/clinical/tooth-chart")
    public String update(@ModelAttribute ToothUpdateForm form, RedirectAttributes ra) {
        try {
            dentalChartService.updateTooth(form);
            ra.addFlashAttribute("successMessage", "Cap nhat trang thai rang thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        if ("examination".equals(form.getReturnTo()) && form.getSessionId() != null) {
            return "redirect:/clinical/examination?sessionId=" + form.getSessionId()
                    + (form.getToothNumber() != null ? "&toothNumber=" + form.getToothNumber() : "");
        }
        return "redirect:/clinical/tooth-chart?patientId=" + form.getPatientId()
                + (form.getSessionId() != null ? "&sessionId=" + form.getSessionId() : "")
                + "&toothNumber=" + form.getToothNumber();
    }
}
