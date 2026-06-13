package com.smartdental.enums;

public enum EmployeeStatus {
    ACTIVE("Đang làm việc"),
    SUSPENDED("Tạm khóa"),
    INACTIVE("Đã nghỉ việc");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
