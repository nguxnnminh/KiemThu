package com.smartdental.enums;

public enum CommonStatus {
    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngừng cung cấp");

    private final String label;

    CommonStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
