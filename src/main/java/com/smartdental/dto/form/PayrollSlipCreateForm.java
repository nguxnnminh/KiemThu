package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollSlipCreateForm {
    private Long doctorId;
    private Integer payrollYear;
    private Integer payrollMonth;
}
