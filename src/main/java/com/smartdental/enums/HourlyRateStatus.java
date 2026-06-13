package com.smartdental.enums;

/**
 * Trang thai muc tien luong theo gio (UC4.1).
 */
public enum HourlyRateStatus {
    ACTIVE("Đang áp dụng"),
    EXPIRED("Hết hiệu lực"),
    CANCELLED("Đã hủy");

    private final String label;

    HourlyRateStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
