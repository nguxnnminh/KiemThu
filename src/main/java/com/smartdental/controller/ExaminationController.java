package com.smartdental.controller;

import com.smartdental.dto.form.ExaminationForm;
import com.smartdental.dto.form.ToothUpdateForm;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.Employee;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.Role;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.enums.ToothStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.ComplexCaseCoefficientService;
import com.smartdental.service.DentalChartService;
import com.smartdental.service.ExaminationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * UC3.2 - Man hinh kham benh: nhap trieu chung, chan doan, ke hoach dieu tri.
 */
@Controller
@RequiredArgsConstructor
public class ExaminationController {

    private final ExaminationService examinationService;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ComplexCaseCoefficientService complexCaseCoefficientService;
    private final DentalChartService dentalChartService;

    @GetMapping("/clinical/examination")
    public String examination(@RequestParam(required = false) Long sessionId,
                               @RequestParam(required = false) Integer toothNumber,
                               Authentication authentication,
                               Model model) {
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.DOCTOR.name()));
        Employee currentDoctor = isDoctor ? currentEmployee(authentication) : null;
        Long doctorId = isDoctor ? (currentDoctor != null ? currentDoctor.getId() : -1L) : null;

        List<VisitCheckin> inExamQueue = examinationService.findInExamQueue(LocalDate.now(), doctorId);
        model.addAttribute("inExamQueue", inExamQueue);
        model.addAttribute("sessionIdsByCheckin", examinationService.mapSessionIdsByAppointment(inExamQueue));

        TreatmentSession session = null;
        if (sessionId != null) {
            session = examinationService.getSessionDetail(sessionId);
            if (isDoctor && (session.getDoctor() == null || !session.getDoctor().getId().equals(doctorId))) {
                throw new BusinessException("Ban khong co quyen xem phien kham nay.");
            }
        }
        model.addAttribute("examSession", session);
        if (session != null) {
            model.addAttribute("invoiceId", invoiceRepository.findByTreatmentSessionId(session.getId())
                    .map(invoice -> invoice.getId()).orElse(null));
            model.addAttribute("chart", dentalChartService.getChart(session.getPatient().getId()));
            model.addAttribute("toothStatuses", ToothStatus.values());

            if (toothNumber != null) {
                model.addAttribute("selectedTooth", toothNumber);
                model.addAttribute("history", dentalChartService.getHistory(session.getPatient().getId(), toothNumber));
            }

            if (!model.containsAttribute("toothForm")) {
                ToothUpdateForm toothForm = new ToothUpdateForm();
                toothForm.setPatientId(session.getPatient().getId());
                toothForm.setSessionId(session.getId());
                toothForm.setToothNumber(toothNumber);
                toothForm.setReturnTo("examination");
                model.addAttribute("toothForm", toothForm);
            }

            if (session.getStatus() == TreatmentSessionStatus.COMPLETED) {
                ComplexCaseCoefficient coefficient = complexCaseCoefficientService
                        .findByTreatmentSessionId(session.getId()).orElse(null);
                model.addAttribute("complexCaseCoefficient", coefficient);
            }
            model.addAttribute("followUpDefaultDate", LocalDate.now().plusMonths(1));
        }

        if (!model.containsAttribute("examinationForm")) {
            ExaminationForm form = new ExaminationForm();
            if (session != null) {
                form.setSessionId(session.getId());
                form.setSymptom(session.getSymptom());
                form.setDiagnosis(session.getDiagnosis());
                form.setTreatmentPlan(session.getTreatmentPlan());
                form.setDoctorNote(session.getDoctorNote());
            }
            model.addAttribute("examinationForm", form);
        }
        return "clinical/examination";
    }

    @PostMapping("/clinical/examination")
    public String save(@ModelAttribute ExaminationForm form, RedirectAttributes ra) {
        try {
            examinationService.saveExamination(form);
            ra.addFlashAttribute("successMessage", "Cap nhat thong tin kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/examination?sessionId=" + form.getSessionId();
    }

    @PostMapping("/clinical/examination/{sessionId}/complete")
    public String complete(@org.springframework.web.bind.annotation.PathVariable Long sessionId,
                            @ModelAttribute ExaminationForm form,
                            RedirectAttributes ra) {
        try {
            form.setSessionId(sessionId);
            examinationService.completeExamination(sessionId, form);
            ra.addFlashAttribute("successMessage", "Hoan tat kham benh thanh cong.");
            return "redirect:/clinical/queue";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clinical/examination?sessionId=" + sessionId;
        }
    }

    private Employee currentEmployee(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .map(com.smartdental.entity.User::getEmployee)
                .orElse(null);
    }
}
