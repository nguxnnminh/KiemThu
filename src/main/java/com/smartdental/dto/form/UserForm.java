package com.smartdental.dto.form;

import com.smartdental.enums.Role;
import com.smartdental.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu them/sua tai khoan (UC1.1).
 */
@Getter
@Setter
public class UserForm {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private Role role;
    private Long employeeId;
    private Long patientId;
    private UserStatus status = UserStatus.ACTIVE;
}
