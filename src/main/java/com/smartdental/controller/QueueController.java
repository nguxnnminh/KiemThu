package com.smartdental.controller;

import com.smartdental.entity.Employee;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.Role;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.ExaminationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * UC3.2 - Hang doi kham (danh sach benh nhan dang cho / dang kham).
 */
@Controller
@RequiredArgsConstructor
public class QueueController {

    private final ExaminationService examinationService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @GetMapping("/clinical/queue")
    public String queue(@RequestParam(required = false) Long doctorId, Authentication authentication, Model model) {
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.DOCTOR.name()));

        Long effectiveDoctorId = doctorId;
        if (isDoctor) {
            Employee currentDoctor = currentEmployee(authentication);
            effectiveDoctorId = currentDoctor != null ? currentDoctor.getId() : -1L;
        }

        LocalDate today = LocalDate.now();
        List<VisitCheckin> waitingQueue = examinationService.findQueue(today, effectiveDoctorId);
        List<VisitCheckin> inExamQueue = examinationService.findInExamQueue(today, effectiveDoctorId);

        model.addAttribute("waitingQueue", waitingQueue);
        model.addAttribute("inExamQueue", inExamQueue);
        model.addAttribute("sessionIdsByCheckin", examinationService.mapSessionIdsByAppointment(inExamQueue));
        model.addAttribute("today", today);
        model.addAttribute("isDoctor", isDoctor);
        model.addAttribute("doctorFilter", doctorId);
        model.addAttribute("doctors", employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE));
        return "clinical/queue";
    }

    @PostMapping("/clinical/queue/{checkinId}/start")
    public String start(@PathVariable Long checkinId, RedirectAttributes ra) {
        try {
            var session = examinationService.startExamination(checkinId);
            ra.addFlashAttribute("successMessage", "Da bat dau kham. Chuyen den man hinh kham benh.");
            return "redirect:/clinical/examination?sessionId=" + session.getId();
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clinical/queue";
        }
    }

    private Employee currentEmployee(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .map(com.smartdental.entity.User::getEmployee)
                .orElse(null);
    }
}
