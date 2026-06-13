package com.smartdental.dto.form;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.HolidayType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Du lieu them/sua ngay nghi (UC2.1).
 */
@Getter
@Setter
public class HolidayForm {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private HolidayType holidayType;
    private String description;
    private CommonStatus status = CommonStatus.ACTIVE;
}
