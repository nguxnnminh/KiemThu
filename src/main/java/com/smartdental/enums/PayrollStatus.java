package com.smartdental.enums;

/**
 * Trang thai phieu luong (UC4.4).
 */
public enum PayrollStatus {
    DRAFT("Bản nháp"),
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã hủy");

    private final String label;

    PayrollStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
