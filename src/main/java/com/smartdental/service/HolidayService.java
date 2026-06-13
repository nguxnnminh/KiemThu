package com.smartdental.service;

import com.smartdental.dto.form.HolidayForm;
import com.smartdental.entity.Holiday;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.HolidayType;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nghiep vu quan ly ngay nghi cua phong kham (UC2.1).
 */
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Holiday> search(String keyword, HolidayType holidayType, CommonStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return holidayRepository.search(kw, holidayType, status, pageable);
    }

    @Transactional(readOnly = true)
    public Holiday getById(Long id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay ngay nghi."));
    }

    @Transactional
    public Holiday create(HolidayForm form) {
        validate(form, null);

        Holiday holiday = new Holiday();
        holiday.setHolidayCode(codeGeneratorService.nextCode(CodePrefix.HOLIDAY));
        applyForm(holiday, form);

        holidayRepository.save(holiday);
        auditLogService.log("CREATE_HOLIDAY", "Holiday", holiday.getHolidayCode(),
                "Tao ngay nghi " + holiday.getName());
        return holiday;
    }

    @Transactional
    public Holiday update(HolidayForm form) {
        Holiday holiday = getById(form.getId());
        validate(form, holiday);

        applyForm(holiday, form);
        holidayRepository.save(holiday);
        auditLogService.log("UPDATE_HOLIDAY", "Holiday", holiday.getHolidayCode(),
                "Cap nhat ngay nghi " + holiday.getName());
        return holiday;
    }

    @Transactional
    public void deactivate(Long id) {
        Holiday holiday = getById(id);
        holiday.setStatus(CommonStatus.INACTIVE);
        holidayRepository.save(holiday);
        auditLogService.log("DEACTIVATE_HOLIDAY", "Holiday", holiday.getHolidayCode(),
                "Ngung su dung ngay nghi " + holiday.getName());
    }

    @Transactional
    public void activate(Long id) {
        Holiday holiday = getById(id);
        holiday.setStatus(CommonStatus.ACTIVE);
        holidayRepository.save(holiday);
        auditLogService.log("ACTIVATE_HOLIDAY", "Holiday", holiday.getHolidayCode(),
                "Kich hoat lai ngay nghi " + holiday.getName());
    }

    private void applyForm(Holiday holiday, HolidayForm form) {
        holiday.setName(form.getName().trim());
        holiday.setStartDate(form.getStartDate());
        holiday.setEndDate(form.getEndDate());
        holiday.setHolidayType(form.getHolidayType());
        holiday.setDescription(form.getDescription());
        if (form.getStatus() != null) {
            holiday.setStatus(form.getStatus());
        }
    }

    private void validate(HolidayForm form, Holiday existing) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten ngay nghi.");
        }
        if (form.getStartDate() == null || form.getEndDate() == null) {
            throw new BusinessException("Vui long nhap ngay bat dau va ngay ket thuc.");
        }
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new BusinessException("Ngay ket thuc phai sau hoac bang ngay bat dau.");
        }
        if (form.getHolidayType() == null) {
            throw new BusinessException("Vui long chon loai ngay nghi.");
        }

        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (holidayRepository.existsByNameIgnoreCase(form.getName().trim())) {
                throw new BusinessException("Ten ngay nghi da ton tai.");
            }
        } else if (holidayRepository.existsByNameIgnoreCaseAndIdNot(form.getName().trim(), currentId)) {
            throw new BusinessException("Ten ngay nghi da ton tai.");
        }
    }
}
