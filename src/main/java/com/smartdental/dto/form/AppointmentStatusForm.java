package com.smartdental.dto.form;

import com.smartdental.enums.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu cap nhat trang thai lich kham (UC2.5).
 */
@Getter
@Setter
public class AppointmentStatusForm {

    private AppointmentStatus status;
    private String note;
    private String cancelReason;
}
