package com.smartdental.service;

import com.smartdental.dto.form.PayrollSlipCreateForm;
import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollItem;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.PayrollStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.PayrollItemRepository;
import com.smartdental.repository.PayrollSlipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * UC4.4 - Lap phieu luong bac si theo thang.
 */
@Service
@RequiredArgsConstructor
public class PayrollSlipService {

    private final PayrollSlipRepository payrollSlipRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollCalculationService payrollCalculationService;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PayrollSlip> search(Integer year, Integer month, Long doctorId, Pageable pageable) {
        return payrollSlipRepository.search(year, month, doctorId, pageable);
    }

    @Transactional(readOnly = true)
    public PayrollSlip getById(Long id) {
        return payrollSlipRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay phieu luong."));
    }

    @Transactional(readOnly = true)
    public List<PayrollItem> getItems(Long slipId) {
        return payrollItemRepository.findBySlipId(slipId);
    }

    @Transactional(readOnly = true)
    public Page<PayrollSlip> findByDoctor(Long doctorId, Pageable pageable) {
        return payrollSlipRepository.search(null, null, doctorId, pageable);
    }

    @Transactional
    public PayrollSlip create(PayrollSlipCreateForm form) {
        if (form.getDoctorId() == null) {
            throw new BusinessException("Vui long chon bac si.");
        }
        if (form.getPayrollYear() == null || form.getPayrollMonth() == null
                || form.getPayrollMonth() < 1 || form.getPayrollMonth() > 12) {
            throw new BusinessException("Vui long chon thang/nam hop le.");
        }

        Employee doctor = employeeRepository.findById(form.getDoctorId())
                .orElseThrow(() -> new BusinessException("Khong tim thay bac si."));
        if (doctor.getPosition() != EmployeePosition.DOCTOR) {
            throw new BusinessException("Chi co the lap phieu luong cho bac si.");
        }

        if (payrollSlipRepository.findByDoctorIdAndPayrollYearAndPayrollMonth(
                doctor.getId(), form.getPayrollYear(), form.getPayrollMonth()).isPresent()) {
            throw new BusinessException("Bac si da co phieu luong cho thang nay.");
        }

        YearMonth period = YearMonth.of(form.getPayrollYear(), form.getPayrollMonth());
        if (payrollCalculationService.hasPendingComplexCoefficient(doctor.getId(), period)) {
            throw new BusinessException("Con he so ca phuc tap dang cho duyet trong thang nay. Vui long xu ly truoc khi lap phieu luong.");
        }

        List<PayrollItem> items = payrollCalculationService.calculateItems(doctor, period);

        PayrollSlip slip = new PayrollSlip();
        slip.setSlipCode(codeGeneratorService.nextCode(CodePrefix.PAYROLL_SLIP));
        slip.setDoctor(doctor);
        slip.setPayrollYear(form.getPayrollYear());
        slip.setPayrollMonth(form.getPayrollMonth());
        slip.setStatus(PayrollStatus.DRAFT);
        slip.setCreatedBy(currentUsername());
        slip.setTotalSalary(sumAmounts(items));
        payrollSlipRepository.save(slip);

        for (PayrollItem item : items) {
            item.setPayrollSlip(slip);
            payrollItemRepository.save(item);
        }

        auditLogService.log("CREATE_PAYROLL_SLIP", "PayrollSlip", slip.getSlipCode(),
                "Lap phieu luong thang " + form.getPayrollMonth() + "/" + form.getPayrollYear() + " cho bac si " + doctor.getFullName());
        return slip;
    }

    @Transactional
    public PayrollSlip recalculate(Long id) {
        PayrollSlip slip = getById(id);
        if (slip.getStatus() != PayrollStatus.DRAFT) {
            throw new BusinessException("Chi co the tinh lai phieu luong o trang thai ban nhap.");
        }

        YearMonth period = YearMonth.of(slip.getPayrollYear(), slip.getPayrollMonth());
        if (payrollCalculationService.hasPendingComplexCoefficient(slip.getDoctor().getId(), period)) {
            throw new BusinessException("Con he so ca phuc tap dang cho duyet trong thang nay. Vui long xu ly truoc khi tinh lai phieu luong.");
        }

        List<PayrollItem> items = payrollCalculationService.calculateItems(slip.getDoctor(), period);

        payrollItemRepository.deleteByPayrollSlipId(slip.getId());
        for (PayrollItem item : items) {
            item.setPayrollSlip(slip);
            payrollItemRepository.save(item);
        }
        slip.setTotalSalary(sumAmounts(items));
        payrollSlipRepository.save(slip);

        auditLogService.log("RECALCULATE_PAYROLL_SLIP", "PayrollSlip", slip.getSlipCode(),
                "Tinh lai phieu luong " + slip.getSlipCode());
        return slip;
    }

    @Transactional
    public PayrollSlip submit(Long id) {
        PayrollSlip slip = getById(id);
        if (slip.getStatus() != PayrollStatus.DRAFT) {
            throw new BusinessException("Chi co the gui duyet phieu luong o trang thai ban nhap.");
        }
        slip.setStatus(PayrollStatus.PENDING);
        payrollSlipRepository.save(slip);

        auditLogService.log("SUBMIT_PAYROLL_SLIP", "PayrollSlip", slip.getSlipCode(),
                "Gui duyet phieu luong " + slip.getSlipCode());
        return slip;
    }

    @Transactional
    public PayrollSlip approve(Long id) {
        PayrollSlip slip = getById(id);
        if (slip.getStatus() != PayrollStatus.PENDING) {
            throw new BusinessException("Chi co the duyet phieu luong o trang thai cho duyet.");
        }
        slip.setStatus(PayrollStatus.APPROVED);
        slip.setApprovedBy(currentUsername());
        payrollSlipRepository.save(slip);

        auditLogService.log("APPROVE_PAYROLL_SLIP", "PayrollSlip", slip.getSlipCode(),
                "Duyet phieu luong " + slip.getSlipCode());
        return slip;
    }

    @Transactional
    public PayrollSlip cancel(Long id) {
        PayrollSlip slip = getById(id);
        if (slip.getStatus() == PayrollStatus.APPROVED) {
            throw new BusinessException("Khong the huy phieu luong da duyet.");
        }
        if (slip.getStatus() == PayrollStatus.CANCELLED) {
            throw new BusinessException("Phieu luong nay da bi huy.");
        }
        slip.setStatus(PayrollStatus.CANCELLED);
        payrollSlipRepository.save(slip);

        auditLogService.log("CANCEL_PAYROLL_SLIP", "PayrollSlip", slip.getSlipCode(),
                "Huy phieu luong " + slip.getSlipCode());
        return slip;
    }

    private BigDecimal sumAmounts(List<PayrollItem> items) {
        return items.stream().map(PayrollItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
