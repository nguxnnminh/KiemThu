package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegisteredServiceForm {
    private Long sessionId;
    private Long serviceId;
    private Integer quantity = 1;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private Integer toothNumber;
    private String note;
}
