package com.smartdental.service;

import com.smartdental.dto.form.DentalServiceForm;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.ServiceCategory;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.ServiceUnit;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Nghiep vu quan ly dich vu nha khoa (UC1.3).
 */
@Service
@RequiredArgsConstructor
public class DentalServiceManagementService {

    private final DentalServiceRepository dentalServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<DentalService> search(String keyword, Long categoryId, ServiceUnit unit, CommonStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return dentalServiceRepository.search(kw, categoryId, unit, status, pageable);
    }

    /** So lieu tong hop hien thi tren stat-row. */
    public record ServiceStats(long total, long active, long categories) {
    }

    @Transactional(readOnly = true)
    public ServiceStats stats() {
        return new ServiceStats(
                dentalServiceRepository.count(),
                dentalServiceRepository.countByStatus(CommonStatus.ACTIVE),
                serviceCategoryRepository.count());
    }

    @Transactional(readOnly = true)
    public DentalService getById(Long id) {
        return dentalServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay dich vu."));
    }

    @Transactional(readOnly = true)
    public List<DentalService> findActive() {
        return dentalServiceRepository.findByStatus(CommonStatus.ACTIVE);
    }

    @Transactional
    public DentalService create(DentalServiceForm form) {
        ServiceCategory category = validate(form, null);

        DentalService service = new DentalService();
        service.setServiceCode(codeGeneratorService.nextCode(CodePrefix.SERVICE));
        applyForm(service, form, category);

        dentalServiceRepository.save(service);
        auditLogService.log("CREATE_SERVICE", "DentalService", service.getServiceCode(),
                "Tao dich vu " + service.getName());
        return service;
    }

    @Transactional
    public DentalService update(DentalServiceForm form) {
        DentalService service = getById(form.getId());
        ServiceCategory category = validate(form, service.getId());

        boolean reactivating = service.getStatus() == CommonStatus.INACTIVE && form.getStatus() == CommonStatus.ACTIVE;
        boolean deactivating = service.getStatus() == CommonStatus.ACTIVE && form.getStatus() == CommonStatus.INACTIVE;

        applyForm(service, form, category);
        dentalServiceRepository.save(service);

        if (deactivating) {
            auditLogService.log("DEACTIVATE_SERVICE", "DentalService", service.getServiceCode(),
                    "Ngung cung cap dich vu " + service.getName());
        } else if (reactivating) {
            auditLogService.log("REACTIVATE_SERVICE", "DentalService", service.getServiceCode(),
                    "Mo lai cung cap dich vu " + service.getName());
        } else {
            auditLogService.log("UPDATE_SERVICE", "DentalService", service.getServiceCode(),
                    "Cap nhat dich vu " + service.getName());
        }
        return service;
    }

    private void applyForm(DentalService service, DentalServiceForm form, ServiceCategory category) {
        service.setName(form.getName().trim());
        service.setCategory(category);
        service.setUnit(form.getUnit());
        service.setDurationMinutes(form.getDurationMinutes());
        service.setDescription(form.getDescription());
        service.setStatus(form.getStatus() == null ? service.getStatus() : form.getStatus());
        if (service.getStatus() == null) {
            service.setStatus(CommonStatus.ACTIVE);
        }
    }

    private ServiceCategory validate(DentalServiceForm form, Long currentId) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten dich vu.");
        }
        if (form.getCategoryId() == null) {
            throw new BusinessException("Vui long chon nhom dich vu.");
        }
        if (form.getUnit() == null) {
            throw new BusinessException("Vui long chon don vi tinh.");
        }
        if (form.getDurationMinutes() == null || form.getDurationMinutes() < 5 || form.getDurationMinutes() > 480) {
            throw new BusinessException("Thoi gian thuc hien phai tu 5 den 480 phut.");
        }

        ServiceCategory category = serviceCategoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new BusinessException("Khong tim thay nhom dich vu duoc chon."));

        boolean duplicated = currentId == null
                ? dentalServiceRepository.existsByNameIgnoreCaseAndCategoryId(form.getName().trim(), category.getId())
                : dentalServiceRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(form.getName().trim(), category.getId(), currentId);
        if (duplicated) {
            throw new BusinessException("Ten dich vu da ton tai trong nhom dich vu nay.");
        }

        return category;
    }
}
