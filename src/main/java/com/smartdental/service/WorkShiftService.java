package com.smartdental.service;

import com.smartdental.dto.form.WorkShiftForm;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.WorkShiftDayType;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nghiep vu quan ly ca lam viec chuan (UC2.2).
 */
@Service
@RequiredArgsConstructor
public class WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<WorkShift> search(String keyword, CommonStatus status, WorkShiftDayType dayType, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return workShiftRepository.search(kw, status, dayType, pageable);
    }

    @Transactional(readOnly = true)
    public List<WorkShift> findActive() {
        return workShiftRepository.findByStatus(CommonStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<WorkShift> findActiveForDate(LocalDate date) {
        WorkShiftDayType dayType = dayTypeOf(date);
        return workShiftRepository.findActiveForDayType(CommonStatus.ACTIVE, dayType);
    }

    @Transactional(readOnly = true)
    public WorkShift getById(Long id) {
        return workShiftRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay ca lam viec."));
    }

    @Transactional
    public WorkShift create(WorkShiftForm form) {
        validate(form, null);

        WorkShift workShift = new WorkShift();
        workShift.setShiftCode(codeGeneratorService.nextCode(CodePrefix.WORK_SHIFT));
        applyForm(workShift, form);

        workShiftRepository.save(workShift);
        auditLogService.log("CREATE_WORK_SHIFT", "WorkShift", workShift.getShiftCode(),
                "Tao ca lam viec " + workShift.getName());
        return workShift;
    }

    @Transactional
    public WorkShift update(WorkShiftForm form) {
        WorkShift workShift = getById(form.getId());
        validate(form, workShift);

        applyForm(workShift, form);
        workShiftRepository.save(workShift);
        auditLogService.log("UPDATE_WORK_SHIFT", "WorkShift", workShift.getShiftCode(),
                "Cap nhat ca lam viec " + workShift.getName());
        return workShift;
    }

    @Transactional
    public void deactivate(Long id) {
        WorkShift workShift = getById(id);
        workShift.setStatus(CommonStatus.INACTIVE);
        workShiftRepository.save(workShift);
        auditLogService.log("DEACTIVATE_WORK_SHIFT", "WorkShift", workShift.getShiftCode(),
                "Ngung su dung ca lam viec " + workShift.getName());
    }

    @Transactional
    public void activate(Long id) {
        WorkShift workShift = getById(id);
        workShift.setStatus(CommonStatus.ACTIVE);
        workShiftRepository.save(workShift);
        auditLogService.log("ACTIVATE_WORK_SHIFT", "WorkShift", workShift.getShiftCode(),
                "Kich hoat lai ca lam viec " + workShift.getName());
    }

    private void applyForm(WorkShift workShift, WorkShiftForm form) {
        workShift.setName(form.getName().trim());
        workShift.setStartTime(form.getStartTime());
        workShift.setEndTime(form.getEndTime());
        workShift.setDayType(form.getDayType() == null ? WorkShiftDayType.ALL : form.getDayType());
        workShift.setDescription(form.getDescription());
        if (form.getStatus() != null) {
            workShift.setStatus(form.getStatus());
        }
    }

    private void validate(WorkShiftForm form, WorkShift existing) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten ca lam viec.");
        }
        if (form.getStartTime() == null || form.getEndTime() == null) {
            throw new BusinessException("Vui long nhap gio bat dau va gio ket thuc.");
        }
        if (!form.getEndTime().isAfter(form.getStartTime())) {
            throw new BusinessException("Gio ket thuc phai sau gio bat dau.");
        }

        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (workShiftRepository.existsByNameIgnoreCase(form.getName().trim())) {
                throw new BusinessException("Ten ca lam viec da ton tai.");
            }
        } else if (workShiftRepository.existsByNameIgnoreCaseAndIdNot(form.getName().trim(), currentId)) {
            throw new BusinessException("Ten ca lam viec da ton tai.");
        }
    }

    public boolean appliesTo(WorkShift workShift, LocalDate date) {
        WorkShiftDayType dayType = workShift.getDayType() == null ? WorkShiftDayType.ALL : workShift.getDayType();
        return dayType.appliesTo(date);
    }

    private WorkShiftDayType dayTypeOf(LocalDate date) {
        if (date == null) {
            return WorkShiftDayType.ALL;
        }
        return WorkShiftDayType.WEEKEND.appliesTo(date) ? WorkShiftDayType.WEEKEND : WorkShiftDayType.WEEKDAY;
    }
}
