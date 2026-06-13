package com.smartdental.enums;

/**
 * Trang thai ban ghi thanh toan / hoan tien (UC3.5).
 */
public enum PaymentStatus {
    SUCCESS("Thành công"),
    CANCELLED("Đã hủy"),
    REFUNDED("Đã hoàn tiền");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
