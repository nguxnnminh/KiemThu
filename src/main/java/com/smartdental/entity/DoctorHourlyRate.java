package com.smartdental.entity;

import com.smartdental.enums.HourlyRateStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Muc tien co ban cho mot gio lam viec cua bac si (UC4.1).
 */
@Getter
@Setter
@Entity
@Table(name = "doctor_hourly_rates")
public class DoctorHourlyRate extends BaseEntity {

    @Column(name = "rate_code", nullable = false, unique = true, length = 20)
    private String rateCode;

    @Column(name = "hourly_rate", nullable = false, precision = 14, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private HourlyRateStatus status = HourlyRateStatus.ACTIVE;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_by", length = 50)
    private String createdBy;
}
