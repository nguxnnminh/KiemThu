package com.smartdental.enums;

/**
 * Phuong thuc thanh toan (UC3.5).
 */
public enum PaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản"),
    CARD("Thẻ");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
