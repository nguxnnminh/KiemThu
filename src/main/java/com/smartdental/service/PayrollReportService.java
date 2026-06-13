package com.smartdental.service;

import com.smartdental.entity.PayrollSlip;
import com.smartdental.repository.PayrollSlipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UC4.5/4.6/4.7 - Bao cao luong bac si theo thang/nam.
 */
@Service
@RequiredArgsConstructor
public class PayrollReportService {

    private final PayrollSlipRepository payrollSlipRepository;

    @Transactional(readOnly = true)
    public List<PayrollSlip> getMonthlyReport(Integer year, Integer month) {
        return payrollSlipRepository.findByYearAndMonth(year, month);
    }

    @Transactional(readOnly = true)
    public List<PayrollSlip> getDoctorYearlyReport(Long doctorId, Integer year) {
        return payrollSlipRepository.findApprovedByDoctorAndYear(doctorId, year);
    }

    @Transactional(readOnly = true)
    public List<PayrollSlip> getYearlyReport(Integer year) {
        return payrollSlipRepository.findApprovedByYear(year);
    }
}
