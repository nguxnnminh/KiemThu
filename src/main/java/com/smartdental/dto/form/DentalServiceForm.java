package com.smartdental.dto.form;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.ServiceUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu them/sua dich vu (UC1.3).
 */
@Getter
@Setter
public class DentalServiceForm {

    private Long id;
    private String name;
    private Long categoryId;
    private ServiceUnit unit;
    private Integer durationMinutes;
    private String description;
    private CommonStatus status = CommonStatus.ACTIVE;
}
