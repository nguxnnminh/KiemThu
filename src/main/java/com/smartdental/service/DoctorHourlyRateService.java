package com.smartdental.service;

import com.smartdental.dto.form.HourlyRateForm;
import com.smartdental.entity.DoctorHourlyRate;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.HourlyRateStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DoctorHourlyRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UC4.1 - Thiet lap muc tien co ban cho mot gio lam viec.
 */
@Service
@RequiredArgsConstructor
public class DoctorHourlyRateService {

    private final DoctorHourlyRateRepository hourlyRateRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<DoctorHourlyRate> findAll() {
        return hourlyRateRepository.findAllByOrderByEffectiveFromDesc();
    }

    @Transactional(readOnly = true)
    public DoctorHourlyRate getById(Long id) {
        return hourlyRateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay muc tien luong."));
    }

    @Transactional(readOnly = true)
    public DoctorHourlyRate getActiveOnDate(LocalDate date) {
        return hourlyRateRepository.findActiveOnDate(date)
                .orElseThrow(() -> new BusinessException("Chua thiet lap muc tien luong/gio cho khoang thoi gian nay."));
    }

    @Transactional
    public DoctorHourlyRate create(HourlyRateForm form) {
        validate(form);
        if (form.getEffectiveFrom().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngay hieu luc khong duoc o qua khu.");
        }

        DoctorHourlyRate rate = new DoctorHourlyRate();
        rate.setRateCode(codeGeneratorService.nextCode(CodePrefix.HOURLY_RATE));
        rate.setHourlyRate(form.getHourlyRate());
        rate.setEffectiveFrom(form.getEffectiveFrom());
        rate.setEffectiveTo(form.getEffectiveTo());
        rate.setStatus(HourlyRateStatus.ACTIVE);
        rate.setNote(form.getNote());
        rate.setCreatedBy(currentUsername());
        hourlyRateRepository.save(rate);

        auditLogService.log("CREATE_HOURLY_RATE", "DoctorHourlyRate", rate.getRateCode(),
                "Thiet lap muc tien luong/gio: " + rate.getHourlyRate());
        return rate;
    }

    @Transactional
    public DoctorHourlyRate cancel(Long id) {
        DoctorHourlyRate rate = getById(id);
        if (rate.getStatus() == HourlyRateStatus.CANCELLED) {
            throw new BusinessException("Muc tien luong nay da bi huy.");
        }
        rate.setStatus(HourlyRateStatus.CANCELLED);
        hourlyRateRepository.save(rate);

        auditLogService.log("CANCEL_HOURLY_RATE", "DoctorHourlyRate", rate.getRateCode(),
                "Huy muc tien luong/gio: " + rate.getRateCode());
        return rate;
    }

    private void validate(HourlyRateForm form) {
        if (form.getHourlyRate() == null || form.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Muc tien luong/gio phai lon hon 0.");
        }
        if (form.getEffectiveFrom() == null) {
            throw new BusinessException("Vui long chon ngay bat dau hieu luc.");
        }
        if (form.getEffectiveTo() != null && form.getEffectiveTo().isBefore(form.getEffectiveFrom())) {
            throw new BusinessException("Ngay ket thuc phai sau ngay bat dau.");
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
