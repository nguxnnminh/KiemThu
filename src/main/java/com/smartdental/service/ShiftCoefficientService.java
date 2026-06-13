package com.smartdental.service;

import com.smartdental.dto.form.ShiftCoefficientForm;
import com.smartdental.entity.ShiftCoefficient;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.ShiftCoefficientStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.ShiftCoefficientRepository;
import com.smartdental.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UC4.2 - Thiet lap he so ca lam viec.
 */
@Service
@RequiredArgsConstructor
public class ShiftCoefficientService {

    private static final BigDecimal MIN_COEFFICIENT = new BigDecimal("1.0");
    private static final BigDecimal MAX_COEFFICIENT = new BigDecimal("1.5");
    private static final BigDecimal DEFAULT_COEFFICIENT = new BigDecimal("1.0");

    private final ShiftCoefficientRepository shiftCoefficientRepository;
    private final WorkShiftRepository workShiftRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ShiftCoefficient> findAll() {
        return shiftCoefficientRepository.findAllWithShift();
    }

    @Transactional(readOnly = true)
    public ShiftCoefficient getById(Long id) {
        return shiftCoefficientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay he so ca."));
    }

    @Transactional(readOnly = true)
    public BigDecimal getCoefficientForShiftOnDate(Long workShiftId, LocalDate date) {
        return shiftCoefficientRepository.findActiveByShiftOnDate(workShiftId, date)
                .map(ShiftCoefficient::getCoefficient)
                .orElse(DEFAULT_COEFFICIENT);
    }

    @Transactional
    public ShiftCoefficient create(ShiftCoefficientForm form) {
        WorkShift workShift = validateAndGetShift(form);

        ShiftCoefficient coefficient = new ShiftCoefficient();
        coefficient.setCoefficientCode(codeGeneratorService.nextCode(CodePrefix.SHIFT_COEFFICIENT));
        coefficient.setWorkShift(workShift);
        coefficient.setCoefficient(form.getCoefficient());
        coefficient.setEffectiveFrom(form.getEffectiveFrom());
        coefficient.setEffectiveTo(form.getEffectiveTo());
        coefficient.setStatus(ShiftCoefficientStatus.ACTIVE);
        coefficient.setNote(form.getNote());
        coefficient.setCreatedBy(currentUsername());
        shiftCoefficientRepository.save(coefficient);

        auditLogService.log("CREATE_SHIFT_COEFFICIENT", "ShiftCoefficient", coefficient.getCoefficientCode(),
                "Thiet lap he so ca " + workShift.getName() + ": " + coefficient.getCoefficient());
        return coefficient;
    }

    @Transactional
    public ShiftCoefficient cancel(Long id) {
        ShiftCoefficient coefficient = getById(id);
        if (coefficient.getStatus() == ShiftCoefficientStatus.CANCELLED) {
            throw new BusinessException("He so ca nay da bi huy.");
        }
        coefficient.setStatus(ShiftCoefficientStatus.CANCELLED);
        shiftCoefficientRepository.save(coefficient);

        auditLogService.log("CANCEL_SHIFT_COEFFICIENT", "ShiftCoefficient", coefficient.getCoefficientCode(),
                "Huy he so ca: " + coefficient.getCoefficientCode());
        return coefficient;
    }

    private WorkShift validateAndGetShift(ShiftCoefficientForm form) {
        if (form.getWorkShiftId() == null) {
            throw new BusinessException("Vui long chon ca lam viec.");
        }
        WorkShift workShift = workShiftRepository.findById(form.getWorkShiftId())
                .orElseThrow(() -> new BusinessException("Khong tim thay ca lam viec."));
        if (form.getCoefficient() == null
                || form.getCoefficient().compareTo(MIN_COEFFICIENT) < 0
                || form.getCoefficient().compareTo(MAX_COEFFICIENT) > 0) {
            throw new BusinessException("He so ca phai trong khoang tu 1.0 den 1.5.");
        }
        if (form.getEffectiveFrom() == null) {
            throw new BusinessException("Vui long chon ngay bat dau hieu luc.");
        }
        if (form.getEffectiveTo() != null && form.getEffectiveTo().isBefore(form.getEffectiveFrom())) {
            throw new BusinessException("Ngay ket thuc phai sau ngay bat dau.");
        }
        return workShift;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
