package com.smartdental.enums;

/**
 * Trang thai dich vu dieu tri da dang ky cho mot phien kham (UC3.4).
 */
public enum RegisteredServiceStatus {
    ACTIVE("Đang hiệu lực"),
    INVOICED("Đã lập hóa đơn"),
    CANCELLED("Đã hủy");

    private final String label;

    RegisteredServiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
