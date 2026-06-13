package com.smartdental.dto.form;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.WorkShiftDayType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Du lieu them/sua ca lam viec (UC2.2).
 */
@Getter
@Setter
public class WorkShiftForm {

    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private WorkShiftDayType dayType = WorkShiftDayType.ALL;
    private String description;
    private CommonStatus status = CommonStatus.ACTIVE;
}
