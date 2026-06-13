package com.smartdental.controller;

import com.smartdental.dto.form.HolidayForm;
import com.smartdental.entity.Holiday;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.HolidayType;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.HolidayService;
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
 * UC2.1 - Quan ly ngay nghi cua phong kham.
 */
@Controller
@RequiredArgsConstructor
public class HolidayController {

    private static final int PAGE_SIZE = 10;

    private final HolidayService holidayService;

    @GetMapping("/schedule/holidays")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) HolidayType holidayType,
                        @RequestParam(required = false) CommonStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<Holiday> holidays = holidayService.search(keyword, holidayType, status, pageable);

        model.addAttribute("holidays", holidays);
        model.addAttribute("keyword", keyword);
        model.addAttribute("holidayTypeFilter", holidayType);
        model.addAttribute("statusFilter", status);
        model.addAttribute("holidayTypes", HolidayType.values());
        model.addAttribute("statuses", CommonStatus.values());

        if (!model.containsAttribute("holidayForm")) {
            model.addAttribute("holidayForm", new HolidayForm());
        }
        return "schedule/holidays";
    }

    @PostMapping("/schedule/holidays")
    public String create(@ModelAttribute HolidayForm form, RedirectAttributes ra) {
        try {
            holidayService.create(form);
            ra.addFlashAttribute("successMessage", "Them ngay nghi thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addHolidayModal");
            ra.addFlashAttribute("holidayForm", form);
        }
        return "redirect:/schedule/holidays";
    }

    @PostMapping("/schedule/holidays/{id}")
    public String update(@PathVariable Long id, @ModelAttribute HolidayForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            holidayService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat ngay nghi thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editHolidayModal-" + id);
            ra.addFlashAttribute("editHolidayForm-" + id, form);
        }
        return "redirect:/schedule/holidays";
    }

    @PostMapping("/schedule/holidays/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            holidayService.deactivate(id);
            ra.addFlashAttribute("successMessage", "Da ngung su dung ngay nghi.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/holidays";
    }

    @PostMapping("/schedule/holidays/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            holidayService.activate(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai ngay nghi.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/holidays";
    }
}
