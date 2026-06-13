package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class HourlyRateForm {
    private Long id;
    private BigDecimal hourlyRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
}
