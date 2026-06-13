package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundForm {
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private String note;
}
