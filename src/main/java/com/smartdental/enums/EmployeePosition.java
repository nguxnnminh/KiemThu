package com.smartdental.enums;

public enum EmployeePosition {
    DOCTOR("Bác sĩ"),
    RECEPTIONIST("Lễ tân"),
    MANAGER("Quản lý");

    private final String label;

    EmployeePosition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
