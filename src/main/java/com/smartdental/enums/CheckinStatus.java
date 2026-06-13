package com.smartdental.enums;

/**
 * Trang thai check-in / hang doi kham (UC3.1).
 */
public enum CheckinStatus {
    WAITING("Đang chờ"),
    CALLED("Đã gọi vào khám"),
    IN_EXAM("Đang khám"),
    DONE("Đã khám xong"),
    CANCELLED("Đã hủy");

    private final String label;

    CheckinStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
