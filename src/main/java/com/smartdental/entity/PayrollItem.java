package com.smartdental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dong chi tiet luong cho moi ca truc da duyet trong phieu luong (UC4.4).
 * Cac gia tri snapshot lai tai thoi diem tinh luong, khong thay doi sau khi phieu duoc duyet.
 */
@Getter
@Setter
@Entity
@Table(name = "payroll_items")
public class PayrollItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_slip_id", nullable = false)
    private PayrollSlip payrollSlip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_shift_registration_id")
    private DoctorShiftRegistration doctorShiftRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_session_id")
    private TreatmentSession treatmentSession;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "shift_name_snapshot", nullable = false, length = 100)
    private String shiftNameSnapshot;

    @Column(name = "total_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "shift_coefficient_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal shiftCoefficientSnapshot;

    @Column(name = "patient_coefficient_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal patientCoefficientSnapshot = BigDecimal.ZERO;

    @Column(name = "converted_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal convertedHours;

    @Column(name = "degree_coefficient_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal degreeCoefficientSnapshot;

    @Column(name = "hourly_rate_snapshot", nullable = false, precision = 14, scale = 2)
    private BigDecimal hourlyRateSnapshot;

    @Column(name = "amount", nullable = false, precision = 16, scale = 2)
    private BigDecimal amount;
}
