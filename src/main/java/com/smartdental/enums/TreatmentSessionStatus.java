package com.smartdental.enums;

/**
 * Trang thai phien kham benh (UC3.2).
 */
public enum TreatmentSessionStatus {
    OPEN("Đang mở"),
    COMPLETED("Hoàn tất"),
    SUSPENDED("Tạm dừng"),
    CANCELLED("Đã hủy");

    private final String label;

    TreatmentSessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
