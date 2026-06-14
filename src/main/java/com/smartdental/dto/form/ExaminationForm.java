package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    /**
     * Lich hen kham lai (tai kham): bac si tich co/khong khi hoan tat phien kham.
     * Neu co, he thong tu tao lich hen tai kham (mac dinh 1 thang sau) cho benh nhan + phong kham.
     */
    private boolean followUp;
    private LocalDate followUpDate;
}
