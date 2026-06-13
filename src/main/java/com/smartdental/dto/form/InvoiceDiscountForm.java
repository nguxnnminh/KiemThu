package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceDiscountForm {
    private Long invoiceId;
    private String discountType;
    private BigDecimal discountValue;
    private String discountNote;
}
