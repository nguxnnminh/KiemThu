package com.smartdental.enums;

/**
 * Loai giao dich thanh toan: thu tien hoac hoan tien (UC3.5).
 */
public enum PaymentType {
    PAYMENT("Thu tiền"),
    REFUND("Hoàn tiền");

    private final String label;

    PaymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
