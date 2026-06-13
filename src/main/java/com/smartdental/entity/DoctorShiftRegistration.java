package com.smartdental.entity;

import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.util.GridLayoutUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dang ky lich truc cua bac si theo ngay + ca (UC2.3 - Dang ky lich truc bac si).
 */
@Getter
@Setter
@Entity
@Table(name = "doctor_shift_registrations")
public class DoctorShiftRegistration extends BaseEntity implements GridLayoutUtil.TimedEvent {

    @Column(name = "registration_code", nullable = false, unique = true, length = 20)
    private String registrationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Employee doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", nullable = false)
    private WorkShift workShift;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chair_id")
    private Chair chair;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private DoctorShiftStatus status = DoctorShiftStatus.REGISTERED;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Override
    public LocalTime getStartTime() {
        return workShift != null ? workShift.getStartTime() : null;
    }

    @Override
    public LocalTime getEndTime() {
        return workShift != null ? workShift.getEndTime() : null;
    }
}
