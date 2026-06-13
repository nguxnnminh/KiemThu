package com.smartdental.controller;

import com.smartdental.dto.form.ServicePriceForm;
import com.smartdental.entity.DentalService;
import com.smartdental.enums.PriceStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.DentalServiceManagementService;
import com.smartdental.service.ServiceCategoryManagementService;
import com.smartdental.service.ServicePriceManagementService;
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
 * UC1.4 - Quan ly bang gia dich vu.
 */
@Controller
@RequiredArgsConstructor
public class ServicePriceController {

    private final ServicePriceManagementService servicePriceManagementService;
    private final DentalServiceManagementService dentalServiceManagementService;
    private final ServiceCategoryManagementService serviceCategoryManagementService;

    @GetMapping("/system/service-prices")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) PriceStatus priceStatus,
                        @RequestParam(required = false) java.time.LocalDate fromDate,
                        @RequestParam(required = false) java.time.LocalDate toDate,
                        Model model) {
        List<DentalService> allServices = dentalServiceManagementService.search(keyword, categoryId, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();

        if (priceStatus != null || fromDate != null || toDate != null) {
            allServices = allServices.stream()
                    .filter(s -> servicePriceManagementService.matchesPriceFilter(s.getId(), priceStatus, fromDate, toDate))
                    .toList();
        }

        model.addAttribute("services", allServices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryFilter", categoryId);
        model.addAttribute("priceStatusFilter", priceStatus);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("categories", serviceCategoryManagementService.findAll());
        model.addAttribute("activeServices", dentalServiceManagementService.findActive());
        model.addAttribute("priceStatuses", PriceStatus.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("countWithoutPrice", servicePriceManagementService.countActiveServicesWithoutCurrentPrice());

        if (!model.containsAttribute("priceForm")) {
            model.addAttribute("priceForm", new ServicePriceForm());
        }
        return "system/service-prices";
    }

    @PostMapping("/system/service-prices")
    public String create(@ModelAttribute ServicePriceForm form, Authentication authentication, RedirectAttributes ra) {
        try {
            servicePriceManagementService.createNewPrice(form, authentication.getName());
            ra.addFlashAttribute("successMessage", "Thiet lap gia moi thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "newPriceModal");
            ra.addFlashAttribute("priceForm", form);
        }
        return "redirect:/system/service-prices";
    }
}
