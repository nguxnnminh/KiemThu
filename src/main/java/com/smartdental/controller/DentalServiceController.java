package com.smartdental.controller;

import com.smartdental.dto.form.DentalServiceForm;
import com.smartdental.dto.form.ServiceCategoryForm;
import com.smartdental.entity.DentalService;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.ServiceUnit;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.DentalServiceManagementService;
import com.smartdental.service.ServiceCategoryManagementService;
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
 * UC1.3 - Quan ly danh muc dich vu (nhom dich vu + dich vu).
 */
@Controller
@RequiredArgsConstructor
public class DentalServiceController {

    private static final int PAGE_SIZE = 10;

    private final DentalServiceManagementService dentalServiceManagementService;
    private final ServiceCategoryManagementService serviceCategoryManagementService;

    @GetMapping("/system/services")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) ServiceUnit unit,
                        @RequestParam(required = false) CommonStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<DentalService> services = dentalServiceManagementService.search(keyword, categoryId, unit, status, pageable);

        model.addAttribute("services", services);
        model.addAttribute("stats", dentalServiceManagementService.stats());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryFilter", categoryId);
        model.addAttribute("unitFilter", unit);
        model.addAttribute("statusFilter", status);
        model.addAttribute("categories", serviceCategoryManagementService.findAll());
        model.addAttribute("activeCategories", serviceCategoryManagementService.findActive());
        model.addAttribute("units", ServiceUnit.values());
        model.addAttribute("statuses", CommonStatus.values());

        if (!model.containsAttribute("serviceForm")) {
            model.addAttribute("serviceForm", new DentalServiceForm());
        }
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new ServiceCategoryForm());
        }
        return "system/services";
    }

    @GetMapping("/system/service-categories")
    public String redirectToServices() {
        return "redirect:/system/services";
    }

    @PostMapping("/system/services")
    public String create(@ModelAttribute DentalServiceForm form, RedirectAttributes ra) {
        try {
            dentalServiceManagementService.create(form);
            ra.addFlashAttribute("successMessage", "Them dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addServiceModal");
            ra.addFlashAttribute("serviceForm", form);
        }
        return "redirect:/system/services";
    }

    @PostMapping("/system/services/{id}")
    public String update(@PathVariable Long id, @ModelAttribute DentalServiceForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            dentalServiceManagementService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editServiceModal-" + id);
            ra.addFlashAttribute("editServiceForm-" + id, form);
        }
        return "redirect:/system/services";
    }

    @PostMapping("/system/service-categories")
    public String createCategory(@ModelAttribute ServiceCategoryForm form, RedirectAttributes ra) {
        try {
            serviceCategoryManagementService.create(form);
            ra.addFlashAttribute("successMessage", "Them nhom dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "categoryModal");
            ra.addFlashAttribute("categoryForm", form);
        }
        ra.addFlashAttribute("reopenModal2", "manageCategoriesModal");
        return "redirect:/system/services";
    }

    @PostMapping("/system/service-categories/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute ServiceCategoryForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            serviceCategoryManagementService.update(form);
            ra.addFlashAttribute("successMessage", "Cap nhat nhom dich vu thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editCategoryModal-" + id);
            ra.addFlashAttribute("editCategoryForm-" + id, form);
        }
        ra.addFlashAttribute("reopenModal2", "manageCategoriesModal");
        return "redirect:/system/services";
    }
}
