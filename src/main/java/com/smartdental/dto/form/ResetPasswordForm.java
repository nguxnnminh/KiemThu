package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu dat lai mat khau (UC1.1).
 */
@Getter
@Setter
public class ResetPasswordForm {

    private String newPassword;
    private String confirmPassword;
}
