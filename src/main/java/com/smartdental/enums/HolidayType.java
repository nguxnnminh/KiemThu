package com.smartdental.enums;

/**
 * Loai ngay nghi (UC2.1).
 */
public enum HolidayType {
    HOLIDAY("Ngày lễ"),
    CLINIC_CLOSED("Phòng khám đóng cửa"),
    MAINTENANCE("Bảo trì");

    private final String label;

    HolidayType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
