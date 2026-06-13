package com.smartdental.enums;

/**
 * Hinh thuc giam gia ap dung cho hoa don (UC3.5).
 */
public enum DiscountType {
    NONE("Không giảm"),
    AMOUNT("Giảm theo số tiền"),
    PERCENT("Giảm theo phần trăm");

    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
