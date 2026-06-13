package com.smartdental.enums;

import java.math.BigDecimal;

/**
 * Hoc vi bac si va he so luong tuong ung (UC4.4).
 */
public enum DoctorDegree {
    BACHELOR(new BigDecimal("1.2"), "Cử nhân"),
    MASTER(new BigDecimal("1.5"), "Thạc sĩ"),
    DOCTOR_PHD(new BigDecimal("2.0"), "Tiến sĩ"),
    ASSOC_PROF(new BigDecimal("2.5"), "Phó giáo sư"),
    PROFESSOR(new BigDecimal("3.0"), "Giáo sư");

    private final BigDecimal coefficient;
    private final String label;

    DoctorDegree(BigDecimal coefficient, String label) {
        this.coefficient = coefficient;
        this.label = label;
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public String getLabel() {
        return label;
    }
}
