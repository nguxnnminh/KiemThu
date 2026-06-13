package com.smartdental.dto.form;

import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Du lieu them/sua nhan vien (UC1.2).
 */
@Getter
@Setter
public class EmployeeForm {

    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private EmployeePosition position;
    private String specialty;
    private String qualification;
    private String workplace;
    private LocalDate hireDate;
    private DoctorDegree degree;
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
