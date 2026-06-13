package com.smartdental.enums;

/**
 * Vai tro nguoi dung trong he thong.
 */
public enum Role {
    ADMIN("Quản trị viên"),
    MANAGER("Quản lý"),
    RECEPTIONIST("Lễ tân"),
    DOCTOR("Bác sĩ"),
    PATIENT("Bệnh nhân");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
