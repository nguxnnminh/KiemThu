package com.smartdental.controller;

import com.smartdental.dto.form.AppointmentForm;
import com.smartdental.dto.form.AppointmentStatusForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.User;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.Gender;
import com.smartdental.enums.Role;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.AppointmentService;
import com.smartdental.service.AppointmentStatusLogService;
import com.smartdental.service.DentalServiceManagementService;
import com.smartdental.service.PatientManagementService;
import com.smartdental.service.WorkShiftService;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.util.GridLayoutUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * UC2.4 - Dang ky lich kham benh nhan, UC2.5 - Theo doi lich kham.
 */
@Controller
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentStatusLogService appointmentStatusLogService;
    private final PatientManagementService patientManagementService;
    private final DentalServiceManagementService dentalServiceManagementService;
    private final WorkShiftService workShiftService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @GetMapping("/schedule/appointments")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) AppointmentStatus status,
                        @RequestParam(required = false) LocalDate weekStart,
                        Model model) {
        LocalDate start = mondayOf(weekStart);
        LocalDate end = start.plusDays(6);
        java.util.List<Appointment> weekAppointments = appointmentService.searchWeek(keyword, doctorId, null, status, start, end);

        model.addAttribute("weekStart", start);
        model.addAttribute("weekEnd", end);
        model.addAttribute("prevWeekStart", start.minusWeeks(1));
        model.addAttribute("nextWeekStart", start.plusWeeks(1));
        model.addAttribute("eventsByDay", groupByDay(weekAppointments, start));
        model.addAttribute("keyword", keyword);
        model.addAttribute("doctorFilter", doctorId);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("doctors", employeeRepository.findByPositionAndStatus(EmployeePosition.DOCTOR, EmployeeStatus.ACTIVE));
        model.addAttribute("services", dentalServiceManagementService.findActive());
        model.addAttribute("workShifts", workShiftService.findActive());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("patients", patientManagementService.search(null, com.smartdental.enums.CommonStatus.ACTIVE,
                PageRequest.of(0, 1000)).getContent());

        if (!model.containsAttribute("appointmentForm")) {
            model.addAttribute("appointmentForm", new AppointmentForm());
        }
        if (!model.containsAttribute("statusForm")) {
            model.addAttribute("statusForm", new AppointmentStatusForm());
        }
        return "schedule/appointments";
    }

    private LocalDate mondayOf(LocalDate date) {
        LocalDate base = date != null ? date : LocalDate.now();
        return base.minusDays(base.getDayOfWeek().getValue() - 1);
    }

    private Map<LocalDate, java.util.List<GridLayoutUtil.Positioned<Appointment>>> groupByDay(
            java.util.List<Appointment> appointments, LocalDate weekStart) {
        Map<LocalDate, java.util.List<GridLayoutUtil.Positioned<Appointment>>> result = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            java.util.List<Appointment> dayEvents = appointments.stream()
                    .filter(a -> a.getAppointmentDate().equals(day))
                    .collect(java.util.stream.Collectors.toList());
            result.put(day, GridLayoutUtil.layout(dayEvents));
        }
        return result;
    }

    @GetMapping("/schedule/appointments/track")
    public String track(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) AppointmentStatus status,
                         @RequestParam(required = false) LocalDate weekStart,
                         Authentication authentication,
                         Model model) {
        boolean isDoctor = hasRole(authentication, Role.DOCTOR);

        Long doctorId = null;
        if (isDoctor) {
            User user = currentUser(authentication);
            doctorId = (user != null && user.getEmployee() != null) ? user.getEmployee().getId() : -1L;
        }

        LocalDate start = mondayOf(weekStart);
        LocalDate end = start.plusDays(6);
        java.util.List<Appointment> weekAppointments = appointmentService.searchWeek(keyword, doctorId, null, status, start, end);

        model.addAttribute("weekStart", start);
        model.addAttribute("weekEnd", end);
        model.addAttribute("prevWeekStart", start.minusWeeks(1));
        model.addAttribute("nextWeekStart", start.plusWeeks(1));
        model.addAttribute("eventsByDay", groupByDay(weekAppointments, start));
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("isDoctor", isDoctor);

        if (!model.containsAttribute("statusForm")) {
            model.addAttribute("statusForm", new AppointmentStatusForm());
        }
        return "schedule/appointment-track";
    }

    @GetMapping("/schedule/appointments/confirmed-shift")
    @ResponseBody
    public Map<String, String> confirmedShift(@RequestParam Long doctorId,
                                                @RequestParam LocalDate date,
                                                @RequestParam Long workShiftId) {
        DoctorShiftRegistration registration = appointmentService.findConfirmedShift(doctorId, date, workShiftId);
        Map<String, String> result = new HashMap<>();
        if (registration != null) {
            result.put("room", registration.getRoom() != null ? registration.getRoom().getName() : null);
            result.put("chair", registration.getChair() != null ? registration.getChair().getName() : null);
        }
        return result;
    }

    @PostMapping("/schedule/appointments")
    public String create(@ModelAttribute AppointmentForm form, RedirectAttributes ra) {
        try {
            appointmentService.create(form, AppointmentSource.RECEPTIONIST);
            ra.addFlashAttribute("successMessage", "Dat lich kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addAppointmentModal");
            ra.addFlashAttribute("appointmentForm", form);
        }
        return "redirect:/schedule/appointments";
    }

    @PostMapping("/schedule/appointments/{id}")
    public String update(@PathVariable Long id, @ModelAttribute AppointmentForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            appointmentService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat lich kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editAppointmentModal-" + id);
            ra.addFlashAttribute("editAppointmentForm-" + id, form);
        }
        return "redirect:/schedule/appointments";
    }

    @PostMapping("/schedule/appointments/{id}/status")
    public String changeStatus(@PathVariable Long id, @ModelAttribute AppointmentStatusForm form, RedirectAttributes ra) {
        try {
            appointmentService.changeStatus(id, form.getStatus(), form.getNote(), form.getCancelReason());
            ra.addFlashAttribute("successMessage", "Cap nhat trang thai lich kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "statusModal-" + id);
        }
        return "redirect:/schedule/appointments";
    }

    @PostMapping("/schedule/appointments/{id}/track-status")
    public String changeStatusFromTrack(@PathVariable Long id, @ModelAttribute AppointmentStatusForm form, RedirectAttributes ra) {
        try {
            appointmentService.changeStatus(id, form.getStatus(), form.getNote(), form.getCancelReason());
            ra.addFlashAttribute("successMessage", "Cap nhat trang thai lich kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "trackStatusModal-" + id);
        }
        return "redirect:/schedule/appointments/track";
    }

    private boolean hasRole(Authentication authentication, Role role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName()).orElse(null);
    }
}
