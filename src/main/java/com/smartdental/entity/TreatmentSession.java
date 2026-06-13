package com.smartdental.entity;

import com.smartdental.enums.TreatmentSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Phien kham benh cua mot lich hen (UC3.2). Day la don vi se duoc Nhom 4
 * dung de de xuat va duyet he so ca kham phuc tap.
 */
@Getter
@Setter
@Entity
@Table(name = "treatment_sessions")
public class TreatmentSession extends BaseEntity {

    @Column(name = "session_code", nullable = false, unique = true, length = 20)
    private String sessionCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Employee doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id", nullable = false)
    private VisitCheckin checkin;

    @Column(name = "examination_date", nullable = false)
    private LocalDate examinationDate;

    @Column(name = "symptom", length = 1000)
    private String symptom;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "treatment_plan", length = 1000)
    private String treatmentPlan;

    @Column(name = "doctor_note", length = 1000)
    private String doctorNote;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private TreatmentSessionStatus status = TreatmentSessionStatus.OPEN;

    @Column(name = "difficult_coefficient", precision = 5, scale = 2)
    private BigDecimal difficultCoefficient;
}
