package com.smartdental.dto.form;

import com.smartdental.enums.CommonStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu them/sua nhom dich vu (UC1.3).
 */
@Getter
@Setter
public class ServiceCategoryForm {

    private Long id;
    private String name;
    private String description;
    private String colorHex;
    private CommonStatus status = CommonStatus.ACTIVE;
}
