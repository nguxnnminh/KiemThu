package com.smartdental.enums;

/**
 * Trang thai he so ca lam viec (UC4.2).
 */
public enum ShiftCoefficientStatus {
    ACTIVE("Đang áp dụng"),
    EXPIRED("Hết hiệu lực"),
    CANCELLED("Đã hủy");

    private final String label;

    ShiftCoefficientStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
