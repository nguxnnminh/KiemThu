package com.smartdental.dto.form;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Du lieu them/sua benh nhan (UC2.6).
 */
@Getter
@Setter
public class PatientForm {

    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private CommonStatus status = CommonStatus.ACTIVE;
}
