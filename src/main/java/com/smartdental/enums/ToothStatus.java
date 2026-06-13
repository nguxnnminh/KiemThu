package com.smartdental.enums;

/**
 * Trang thai rang theo so do FDI 32 rang (UC3.3).
 */
public enum ToothStatus {
    NORMAL("Bình thường"),
    CARIES("Sâu răng"),
    FILLED("Đã trám"),
    EXTRACTED("Đã nhổ"),
    CROWNED("Đã bọc răng sứ"),
    MISSING("Mất răng"),
    ROOT_CANAL("Đã điều trị tủy"),
    WATCH("Cần theo dõi"),
    IMPLANT("Cấy ghép Implant"),
    WISDOM_ANGLED("Răng khôn mọc lệch"),
    BROKEN("Răng vỡ/mẻ");

    private final String label;

    ToothStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Mau hien thi tren so do rang. */
    public String getColorClass() {
        return switch (this) {
            case NORMAL -> "tooth-normal";
            case CARIES -> "tooth-caries";
            case FILLED -> "tooth-filled";
            case EXTRACTED, MISSING -> "tooth-missing";
            case CROWNED, IMPLANT -> "tooth-crowned";
            case ROOT_CANAL -> "tooth-root-canal";
            case WATCH -> "tooth-watch";
            case WISDOM_ANGLED -> "tooth-wisdom";
            case BROKEN -> "tooth-broken";
        };
    }
}
