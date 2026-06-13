package com.smartdental.entity;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.WorkShiftDayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;

/**
 * Ca lam viec chuan cua phong kham (UC2.2).
 */
@Getter
@Setter
@Entity
@Table(name = "work_shifts")
public class WorkShift extends BaseEntity {

    @Column(name = "shift_code", nullable = false, unique = true, length = 20)
    private String shiftCode;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "day_type", nullable = false, length = 20)
    private WorkShiftDayType dayType = WorkShiftDayType.ALL;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "max_appointments", nullable = false)
    private Integer maxAppointments = 10;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private CommonStatus status = CommonStatus.ACTIVE;
}
