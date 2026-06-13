package com.smartdental.dto.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToothUpdateForm {
    private Long patientId;
    private Long sessionId;
    private Integer toothNumber;
    private String status;
    private String diagnosis;
    private String note;
    private String returnTo;
}
