package com.smartdental.controller;

import com.smartdental.dto.form.CheckinForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.service.CheckinService;
import com.smartdental.service.WorkShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * UC3.1 - Tiep don benh nhan (check-in, hang doi kham).
 */
@Controller
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftService workShiftService;

    @GetMapping("/clinical/checkin")
    public String checkin(@RequestParam(required = false) Long doctorId,
                           @RequestParam(required = false) Long workShiftId,
                           @RequestParam(required = false) String keyword,
                           Model model) {
        LocalDate today = LocalDate.now();
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        List<Appointment> appointments = checkinService.findConfirmedForCheckin(today, doctorId, workShiftId, kw);
        List<VisitCheckin> waitingQueue = checkinService.findQueue(today, CheckinStatus.WAITING, null);

        model.addAttribute("appointments", appointments);
        model.addAttribute("waitingQueue", waitingQueue);
        model.addAttribute("today", today);
        model.addAttribute("doctorFilter", doctorId);
        model.addAttribute("workShiftFilter", workShiftId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("doctors", employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE));
        model.addAttribute("workShifts", workShiftService.findActive());
        model.addAttribute("arrivalStatuses", ArrivalStatus.values());

        if (!model.containsAttribute("checkinForm")) {
            model.addAttribute("checkinForm", new CheckinForm());
        }
        return "clinical/checkin";
    }

    @PostMapping("/clinical/checkin")
    public String doCheckin(@ModelAttribute CheckinForm form, RedirectAttributes ra) {
        try {
            VisitCheckin checkin = checkinService.checkin(form);
            ra.addFlashAttribute("successMessage",
                    "Check-in thanh cong. So thu tu: " + checkin.getQueueNumber());
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "checkinModal-" + form.getAppointmentId());
        }
        return "redirect:/clinical/checkin";
    }

    @PostMapping("/clinical/checkin/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam String reason, RedirectAttributes ra) {
        try {
            checkinService.cancel(id, reason);
            ra.addFlashAttribute("successMessage", "Huy check-in thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "cancelCheckinModal-" + id);
        }
        return "redirect:/clinical/checkin";
    }
}
