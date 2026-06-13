package com.smartdental.enums;

/**
 * Trang thai phong/ghe (UC2.2b - Quan ly phong & ghe).
 */
public enum RoomStatus {
    ACTIVE("Đang sử dụng"),
    MAINTENANCE("Bảo trì"),
    INACTIVE("Ngừng sử dụng");

    private final String label;

    RoomStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
