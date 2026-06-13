package com.smartdental.entity;

import com.smartdental.enums.ApprovalStatus;
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

import java.math.BigDecimal;

/**
 * He so ca kham phuc tap de xuat cho mot phien dieu tri (UC4.3).
 */
@Getter
@Setter
@Entity
@Table(name = "complex_case_coefficients")
public class ComplexCaseCoefficient extends BaseEntity {

    @Column(name = "coefficient_code", nullable = false, unique = true, length = 20)
    private String coefficientCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_session_id", nullable = false)
    private TreatmentSession treatmentSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Employee doctor;

    @Column(name = "coefficient", nullable = false, precision = 5, scale = 2)
    private BigDecimal coefficient;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "proposed_by", length = 50)
    private String proposedBy;
}
