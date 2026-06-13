package com.smartdental.enums;

/**
 * Trang thai hoa don (UC3.5).
 */
public enum InvoiceStatus {
    UNPAID("Chưa thanh toán"),
    PARTIAL("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
