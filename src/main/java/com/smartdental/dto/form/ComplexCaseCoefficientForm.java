package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ComplexCaseCoefficientForm {
    private Long treatmentSessionId;
    private BigDecimal coefficient;
    private String reason;
}
