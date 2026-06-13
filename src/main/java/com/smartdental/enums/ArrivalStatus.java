package com.smartdental.enums;

/**
 * Tinh trang den kham so voi gio hen (UC3.1).
 */
public enum ArrivalStatus {
    ON_TIME("Đúng giờ"),
    LATE("Đến muộn");

    private final String label;

    ArrivalStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
