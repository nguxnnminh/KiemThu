package com.smartdental.enums;

/**
 * Trang thai lich kham (UC2.4, UC2.5).
 * COMPLETED, CANCELLED, NO_SHOW la trang thai cuoi.
 */
public enum AppointmentStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    CHECKED_IN("Đã check-in"),
    IN_PROGRESS("Đang khám"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy"),
    NO_SHOW("Không đến");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
