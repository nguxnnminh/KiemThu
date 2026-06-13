package com.smartdental.entity;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.HolidayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

/**
 * Ngay nghi / lich nghi cua phong kham (UC2.1).
 */
@Getter
@Setter
@Entity
@Table(name = "holidays")
public class Holiday extends BaseEntity {

    @Column(name = "holiday_code", nullable = false, unique = true, length = 20)
    private String holidayCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "holiday_type", nullable = false, length = 20)
    private HolidayType holidayType;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private CommonStatus status = CommonStatus.ACTIVE;
}
