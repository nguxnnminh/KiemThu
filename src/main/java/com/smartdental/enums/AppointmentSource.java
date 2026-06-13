package com.smartdental.enums;

/**
 * Nguon tao lich kham (UC2.5).
 */
public enum AppointmentSource {
    RECEPTIONIST("Lễ tân"),
    PATIENT_ONLINE("Bệnh nhân đặt online");

    private final String label;

    AppointmentSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
