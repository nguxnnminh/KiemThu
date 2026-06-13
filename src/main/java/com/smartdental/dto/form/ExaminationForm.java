package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Du lieu kham benh: trieu chung, chan doan, ke hoach dieu tri (UC3.2).
 */
@Getter
@Setter
public class ExaminationForm {

    private Long sessionId;
    private String symptom;
    private String diagnosis;
    private String treatmentPlan;
    private String doctorNote;

    /**
     * He so ca phuc tap nhap kem theo khi hoan tat kham (UC4.3).
     * Chi duoc su dung/tao ComplexCaseCoefficient khi hoan tat kham, khong luu khi luu nhap.
     */
    private BigDecimal complexCaseCoefficient;
    private String complexCaseReason;
}
