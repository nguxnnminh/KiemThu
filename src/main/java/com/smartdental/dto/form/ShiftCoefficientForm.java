package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ShiftCoefficientForm {
    private Long id;
    private Long workShiftId;
    private BigDecimal coefficient;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
}
