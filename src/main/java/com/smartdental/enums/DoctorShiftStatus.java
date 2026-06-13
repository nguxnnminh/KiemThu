package com.smartdental.enums;

/**
 * Trang thai dang ky lich truc bac si (UC2.3 - Dang ky lich truc bac si).
 */
public enum DoctorShiftStatus {
    REGISTERED("Đã đăng ký"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy");

    private final String label;

    DoctorShiftStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
