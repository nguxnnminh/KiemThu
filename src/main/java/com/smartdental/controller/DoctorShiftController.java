package com.smartdental.controller;

import com.smartdental.dto.form.DoctorShiftDecisionForm;
import com.smartdental.dto.form.DoctorShiftRegistrationForm;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.Role;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.DoctorShiftService;
import com.smartdental.service.RoomChairService;
import com.smartdental.service.WorkShiftService;
import com.smartdental.util.GridLayoutUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UC2.3 - Dang ky lich truc bac si.
 */
@Controller
@RequiredArgsConstructor
public class DoctorShiftController {

    private final DoctorShiftService doctorShiftService;
    private final WorkShiftService workShiftService;
    private final RoomChairService roomChairService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @GetMapping("/schedule/doctor-shifts")
    public String list(@RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) DoctorShiftStatus status,
                        @RequestParam(required = false) LocalDate weekStart,
                        Authentication authentication,
                        Model model) {
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.DOCTOR.name()));

        Long effectiveDoctorId = doctorId;
        if (isDoctor) {
            Employee currentDoctor = currentDoctor(authentication);
            effectiveDoctorId = currentDoctor != null ? currentDoctor.getId() : -1L;
        }

        LocalDate start = mondayOf(weekStart);
        LocalDate end = start.plusDays(6);
        List<DoctorShiftRegistration> weekRegistrations = doctorShiftService.searchWeek(effectiveDoctorId, start, end, status);

        model.addAttribute("weekStart", start);
        model.addAttribute("weekEnd", end);
        model.addAttribute("prevWeekStart", start.minusWeeks(1));
        model.addAttribute("nextWeekStart", start.plusWeeks(1));
        model.addAttribute("eventsByDay", groupByDay(weekRegistrations, start));
        model.addAttribute("doctorFilter", doctorId);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", DoctorShiftStatus.values());
        model.addAttribute("isDoctor", isDoctor);
        model.addAttribute("doctors", employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE));
        model.addAttribute("workShifts", workShiftService.findActive());
        model.addAttribute("rooms", roomChairService.findActiveRooms());
        model.addAttribute("chairs", roomChairService.findActiveChairs());

        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new DoctorShiftRegistrationForm());
        }
        if (!model.containsAttribute("decisionForm")) {
            model.addAttribute("decisionForm", new DoctorShiftDecisionForm());
        }
        return "schedule/doctor-shifts";
    }

    private LocalDate mondayOf(LocalDate date) {
        LocalDate base = date != null ? date : LocalDate.now();
        return base.minusDays(base.getDayOfWeek().getValue() - 1);
    }

    private Map<LocalDate, List<GridLayoutUtil.Positioned<DoctorShiftRegistration>>> groupByDay(
            List<DoctorShiftRegistration> registrations, LocalDate weekStart) {
        Map<LocalDate, List<GridLayoutUtil.Positioned<DoctorShiftRegistration>>> result = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<DoctorShiftRegistration> dayEvents = registrations.stream()
                    .filter(r -> r.getWorkDate().equals(day))
                    .collect(java.util.stream.Collectors.toList());
            result.put(day, GridLayoutUtil.layout(dayEvents));
        }
        return result;
    }

    @PostMapping("/schedule/doctor-shifts")
    public String register(@ModelAttribute DoctorShiftRegistrationForm form, Authentication authentication, RedirectAttributes ra) {
        try {
            boolean isDoctor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.DOCTOR.name()));
            if (isDoctor) {
                Employee currentDoctor = currentDoctor(authentication);
                if (currentDoctor == null) {
                    throw new BusinessException("Không tìm thấy thông tin bác sĩ của tài khoản hiện tại.");
                }
                form.setDoctorId(currentDoctor.getId());
            }
            doctorShiftService.register(form);
            ra.addFlashAttribute("successMessage", "Dang ky lich truc thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addRegistrationModal");
            ra.addFlashAttribute("registrationForm", form);
        }
        return "redirect:/schedule/doctor-shifts";
    }

    @PostMapping("/schedule/doctor-shifts/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            doctorShiftService.cancel(id);
            ra.addFlashAttribute("successMessage", "Da huy dang ky lich truc.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/doctor-shifts";
    }

    @PostMapping("/schedule/doctor-shifts/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            doctorShiftService.approve(id);
            ra.addFlashAttribute("successMessage", "Da duyet lich truc.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/doctor-shifts";
    }

    @PostMapping("/schedule/doctor-shifts/{id}/reject")
    public String reject(@PathVariable Long id, @ModelAttribute DoctorShiftDecisionForm form, RedirectAttributes ra) {
        try {
            doctorShiftService.reject(id, form.getReason());
            ra.addFlashAttribute("successMessage", "Da tu choi lich truc.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "rejectRegistrationModal-" + id);
        }
        return "redirect:/schedule/doctor-shifts";
    }

    private Employee currentDoctor(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .map(com.smartdental.entity.User::getEmployee)
                .orElse(null);
    }
}
