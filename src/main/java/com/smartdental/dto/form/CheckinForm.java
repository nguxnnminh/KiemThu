package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu check-in benh nhan cho lich hen (UC3.1).
 */
@Getter
@Setter
public class CheckinForm {

    private Long appointmentId;
    private String arrivalStatus;
    private String initialSymptoms;
    private String note;
}
