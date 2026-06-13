package com.smartdental.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Tong hop doanh thu theo phuong thuc thanh toan hoac theo ngay (UC3.6).
 */
@Getter
@AllArgsConstructor
public class RevenueSummary {
    private String label;
    private BigDecimal totalPayments;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
}
