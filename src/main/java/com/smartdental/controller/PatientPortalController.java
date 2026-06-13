package com.smartdental.controller;

import com.smartdental.dto.form.AppointmentForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.User;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.UserRepository;
import com.smartdental.service.AppointmentService;
import com.smartdental.service.DentalServiceManagementService;
import com.smartdental.service.InvoiceService;
import com.smartdental.service.WorkShiftService;
import com.smartdental.util.GridLayoutUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Khu vuc benh nhan: dat lich online va xem lich hen cua minh (UC2.4 - dat lich, UC2.5 - xem lich cua minh).
 */
@Controller
@RequiredArgsConstructor
public class PatientPortalController {

    private static final int PAGE_SIZE = 10;

    private final AppointmentService appointmentService;
    private final DentalServiceManagementService dentalServiceManagementService;
    private final WorkShiftService workShiftService;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;

    @GetMapping("/patient/book-appointment")
    public String bookForm(Model model) {
        model.addAttribute("services", dentalServiceManagementService.findActive());
        model.addAttribute("workShifts", workShiftService.findActive());
        if (!model.containsAttribute("appointmentForm")) {
            model.addAttribute("appointmentForm", new AppointmentForm());
        }
        return "patient/book-appointment";
    }

    @PostMapping("/patient/book-appointment")
    public String book(@ModelAttribute AppointmentForm form, Authentication authentication, RedirectAttributes ra) {
        try {
            User user = userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                    .orElseThrow(() -> new BusinessException("Khong tim thay tai khoan."));
            if (user.getPatient() == null) {
                throw new BusinessException("Tai khoan chua duoc lien ket voi ho so benh nhan.");
            }
            form.setPatientId(user.getPatient().getId());
            appointmentService.create(form, AppointmentSource.PATIENT_ONLINE);
            ra.addFlashAttribute("successMessage", "Dat lich kham thanh cong. Vui long cho xac nhan.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("appointmentForm", form);
        }
        return "redirect:/patient/book-appointment";
    }

    @GetMapping("/patient/appointments")
    public String myAppointments(@RequestParam(required = false) AppointmentStatus status,
                                  @RequestParam(required = false) LocalDate weekStart,
                                  Authentication authentication,
                                  Model model) {
        User user = userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .orElse(null);
        Long patientId = (user != null && user.getPatient() != null) ? user.getPatient().getId() : -1L;

        LocalDate start = mondayOf(weekStart);
        LocalDate end = start.plusDays(6);
        List<Appointment> weekAppointments = appointmentService.searchWeek(null, null, patientId, status, start, end);

        model.addAttribute("weekStart", start);
        model.addAttribute("weekEnd", end);
        model.addAttribute("prevWeekStart", start.minusWeeks(1));
        model.addAttribute("nextWeekStart", start.plusWeeks(1));
        model.addAttribute("eventsByDay", groupByDay(weekAppointments, start));
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", AppointmentStatus.values());
        return "patient/appointments";
    }

    private LocalDate mondayOf(LocalDate date) {
        LocalDate base = date != null ? date : LocalDate.now();
        return base.minusDays(base.getDayOfWeek().getValue() - 1);
    }

    private Map<LocalDate, List<GridLayoutUtil.Positioned<Appointment>>> groupByDay(
            List<Appointment> appointments, LocalDate weekStart) {
        Map<LocalDate, List<GridLayoutUtil.Positioned<Appointment>>> result = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Appointment> dayEvents = appointments.stream()
                    .filter(a -> a.getAppointmentDate().equals(day))
                    .collect(java.util.stream.Collectors.toList());
            result.put(day, GridLayoutUtil.layout(dayEvents));
        }
        return result;
    }

    @GetMapping("/patient/invoices")
    public String myInvoices(@RequestParam(defaultValue = "0") int page,
                              Authentication authentication,
                              Model model) {
        User user = userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .orElse(null);
        Long patientId = (user != null && user.getPatient() != null) ? user.getPatient().getId() : -1L;

        Page<Invoice> invoices = invoiceService.findByPatient(patientId, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        model.addAttribute("invoices", invoices);
        return "patient/invoices";
    }
}
