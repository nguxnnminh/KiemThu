package com.smartdental.controller;

import com.smartdental.dto.form.WorkShiftForm;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.WorkShiftDayType;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.WorkShiftService;
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
 * UC2.2 - Quan ly ca lam viec chuan.
 */
@Controller
@RequiredArgsConstructor
public class WorkShiftController {

    private static final int PAGE_SIZE = 10;

    private final WorkShiftService workShiftService;

    @GetMapping("/schedule/work-shifts")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) CommonStatus status,
                        @RequestParam(required = false) WorkShiftDayType dayType,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<WorkShift> workShifts = workShiftService.search(keyword, status, dayType, pageable);

        model.addAttribute("workShifts", workShifts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", status);
        model.addAttribute("dayTypeFilter", dayType);
        model.addAttribute("statuses", CommonStatus.values());
        model.addAttribute("dayTypes", WorkShiftDayType.values());

        if (!model.containsAttribute("workShiftForm")) {
            model.addAttribute("workShiftForm", new WorkShiftForm());
        }
        return "schedule/work-shifts";
    }

    @PostMapping("/schedule/work-shifts")
    public String create(@ModelAttribute WorkShiftForm form, RedirectAttributes ra) {
        try {
            workShiftService.create(form);
            ra.addFlashAttribute("successMessage", "Them ca lam viec thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addWorkShiftModal");
            ra.addFlashAttribute("workShiftForm", form);
        }
        return "redirect:/schedule/work-shifts";
    }

    @PostMapping("/schedule/work-shifts/{id}")
    public String update(@PathVariable Long id, @ModelAttribute WorkShiftForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            workShiftService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat ca lam viec thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editWorkShiftModal-" + id);
            ra.addFlashAttribute("editWorkShiftForm-" + id, form);
        }
        return "redirect:/schedule/work-shifts";
    }

    @PostMapping("/schedule/work-shifts/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            workShiftService.deactivate(id);
            ra.addFlashAttribute("successMessage", "Da ngung su dung ca lam viec.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/work-shifts";
    }

    @PostMapping("/schedule/work-shifts/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            workShiftService.activate(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai ca lam viec.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/work-shifts";
    }
}
