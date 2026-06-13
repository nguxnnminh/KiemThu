package com.smartdental.repository;

import com.smartdental.entity.DoctorHourlyRate;
import com.smartdental.enums.HourlyRateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorHourlyRateRepository extends JpaRepository<DoctorHourlyRate, Long> {

    List<DoctorHourlyRate> findAllByOrderByEffectiveFromDesc();

    @Query("select r from DoctorHourlyRate r where r.status = com.smartdental.enums.HourlyRateStatus.ACTIVE " +
            "and r.effectiveFrom <= :date and (r.effectiveTo is null or r.effectiveTo >= :date)")
    Optional<DoctorHourlyRate> findActiveOnDate(@Param("date") LocalDate date);

    List<DoctorHourlyRate> findByStatus(HourlyRateStatus status);
}
