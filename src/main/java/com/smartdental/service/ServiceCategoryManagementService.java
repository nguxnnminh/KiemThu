package com.smartdental.service;

import com.smartdental.dto.form.ServiceCategoryForm;
import com.smartdental.entity.ServiceCategory;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Nghiep vu quan ly nhom dich vu (UC1.3).
 */
@Service
@RequiredArgsConstructor
public class ServiceCategoryManagementService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ServiceCategory> findAll() {
        return serviceCategoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<ServiceCategory> findActive() {
        return serviceCategoryRepository.findByStatus(CommonStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ServiceCategory getById(Long id) {
        return serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay nhom dich vu."));
    }

    @Transactional
    public ServiceCategory create(ServiceCategoryForm form) {
        validate(form, null);

        ServiceCategory category = new ServiceCategory();
        category.setCategoryCode(codeGeneratorService.nextCode(CodePrefix.SERVICE_CATEGORY));
        category.setName(form.getName().trim());
        category.setDescription(form.getDescription());
        category.setColorHex(form.getColorHex());
        category.setStatus(form.getStatus() == null ? CommonStatus.ACTIVE : form.getStatus());

        serviceCategoryRepository.save(category);
        auditLogService.log("CREATE_SERVICE_CATEGORY", "ServiceCategory", category.getCategoryCode(),
                "Tao nhom dich vu " + category.getName());
        return category;
    }

    @Transactional
    public ServiceCategory update(ServiceCategoryForm form) {
        ServiceCategory category = getById(form.getId());
        validate(form, category.getId());

        if (category.getStatus() == CommonStatus.ACTIVE && form.getStatus() == CommonStatus.INACTIVE) {
            long activeServices = dentalServiceRepository.countByCategoryIdAndStatus(category.getId(), CommonStatus.ACTIVE);
            if (activeServices > 0) {
                throw new BusinessException("Khong the ngung hoat dong nhom dich vu vi van con dich vu dang hoat dong thuoc nhom nay.");
            }
            auditLogService.log("DEACTIVATE_SERVICE_CATEGORY", "ServiceCategory", category.getCategoryCode(),
                    "Ngung hoat dong nhom dich vu " + category.getName());
        }

        category.setName(form.getName().trim());
        category.setDescription(form.getDescription());
        category.setColorHex(form.getColorHex());
        category.setStatus(form.getStatus() == null ? category.getStatus() : form.getStatus());

        serviceCategoryRepository.save(category);
        auditLogService.log("UPDATE_SERVICE_CATEGORY", "ServiceCategory", category.getCategoryCode(),
                "Cap nhat nhom dich vu " + category.getName());
        return category;
    }

    private void validate(ServiceCategoryForm form, Long currentId) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten nhom dich vu.");
        }
        boolean duplicated = currentId == null
                ? serviceCategoryRepository.existsByNameIgnoreCase(form.getName().trim())
                : serviceCategoryRepository.existsByNameIgnoreCaseAndIdNot(form.getName().trim(), currentId);
        if (duplicated) {
            throw new BusinessException("Ten nhom dich vu da ton tai.");
        }
    }
}
