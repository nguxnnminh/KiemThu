package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Du lieu thiet lap gia moi cho dich vu (UC1.4).
 */
@Getter
@Setter
public class ServicePriceForm {

    private Long serviceId;
    private BigDecimal price;
    private LocalDate effectiveFrom;
    private String reason;
}
