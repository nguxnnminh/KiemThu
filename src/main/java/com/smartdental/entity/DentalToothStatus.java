package com.smartdental.entity;

import com.smartdental.enums.ToothStatus;
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

/**
 * Trang thai hien hanh cua mot rang theo so do FDI 32 cua benh nhan (UC3.3).
 * Moi benh nhan chi co mot ban ghi cho moi so rang.
 */
@Getter
@Setter
@Entity
@Table(name = "dental_tooth_status")
public class DentalToothStatus extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    @Column(name = "tooth_name", length = 50)
    private String toothName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private ToothStatus status = ToothStatus.NORMAL;

    @Column(name = "diagnosis", length = 255)
    private String diagnosis;

    @Column(name = "note", length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_session_id")
    private TreatmentSession lastUpdatedSession;
}
