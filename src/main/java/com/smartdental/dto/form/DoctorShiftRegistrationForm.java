package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Du lieu dang ky lich truc bac si (UC2.3 - Dang ky lich truc bac si).
 */
@Getter
@Setter
public class DoctorShiftRegistrationForm {

    private Long id;
    private Long doctorId;
    private Long workShiftId;
    private LocalDate workDate;
    private Long roomId;
    private Long chairId;
    private String note;
}
