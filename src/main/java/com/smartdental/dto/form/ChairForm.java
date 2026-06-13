package com.smartdental.dto.form;

import com.smartdental.enums.RoomStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Du lieu them/sua ghe nha khoa (UC2.2b - Quan ly phong & ghe).
 */
@Getter
@Setter
public class ChairForm {

    private Long id;
    private String name;
    private Long roomId;
    private String description;
    private RoomStatus status = RoomStatus.ACTIVE;
}
