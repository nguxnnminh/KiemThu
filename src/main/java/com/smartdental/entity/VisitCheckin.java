package com.smartdental.entity;

import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
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
import java.time.LocalDateTime;

/**
 * Tiep don / check-in benh nhan cho mot lich kham (UC3.1).
 */
@Getter
@Setter
@Entity
@Table(name = "visit_checkins")
public class VisitCheckin extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionist_id")
    private Employee receptionist;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "arrival_status", nullable = false, length = 20)
    private ArrivalStatus arrivalStatus;

    @Column(name = "initial_symptoms", length = 500)
    private String initialSymptoms;

    @Column(name = "note", length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private CheckinStatus status = CheckinStatus.WAITING;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;
}
