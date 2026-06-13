package com.smartdental.enums;

/**
 * Trang thai cua mot ban ghi gia dich vu (UC1.4).
 */
public enum PriceStatus {
    ACTIVE("Đang áp dụng"),
    EXPIRED("Hết hiệu lực"),
    CANCELLED("Đã hủy");

    private final String label;

    PriceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
