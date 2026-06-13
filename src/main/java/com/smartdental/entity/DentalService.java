package com.smartdental.entity;

import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.ServiceUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Dich vu nha khoa (UC1.3). Khong dat ten "Service" de tranh xung dot voi @Service cua Spring.
 */
@Getter
@Setter
@Entity
@Table(name = "services")
public class DentalService extends BaseEntity {

    @Column(name = "service_code", nullable = false, unique = true, length = 20)
    private String serviceCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "unit", nullable = false, length = 20)
    private ServiceUnit unit;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private CommonStatus status = CommonStatus.ACTIVE;
}
