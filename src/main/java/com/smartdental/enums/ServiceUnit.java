package com.smartdental.enums;

/**
 * Don vi tinh cua dich vu nha khoa (UC1.3).
 */
public enum ServiceUnit {
    LAN("Lần"),
    RANG("Răng"),
    HAM("Hàm"),
    LIEU_TRINH("Liệu trình"),
    PHIM("Phim");

    private final String label;

    ServiceUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
