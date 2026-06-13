package com.smartdental.dto.form;

import com.smartdental.enums.RoomStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu them/sua phong kham (UC2.2b - Quan ly phong & ghe).
 */
@Getter
@Setter
public class RoomForm {

    private Long id;
    private String name;
    private String description;
    private RoomStatus status = RoomStatus.ACTIVE;
}
