package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Du lieu dat / sua lich kham (UC2.4 - Dang ky lich kham benh nhan).
 * Phong/ghe khong duoc chon truc tiep, he thong tu ke thua tu lich truc bac si CONFIRMED.
 */
@Getter
@Setter
public class AppointmentForm {

    private Long id;
    private Long patientId;

    // Cho benh nhan moi (dat lich online / le tan tao moi)
    private String patientFullName;
    private String patientPhone;
    private String patientDateOfBirth;
    private String patientGender;
    private String patientEmail;
    private String patientAddress;

    private Long doctorId;
    private Long serviceId;
    private Long workShiftId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
}
