package com.smartdental.repository;

import com.smartdental.entity.ServicePrice;
import com.smartdental.enums.PriceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {

    Optional<ServicePrice> findByDentalServiceIdAndStatus(Long serviceId, PriceStatus status);

    List<ServicePrice> findByDentalServiceIdOrderByEffectiveFromDesc(Long serviceId);

    @Query("select case when count(p) > 0 then true else false end from ServicePrice p " +
            "where p.dentalService.id = :serviceId and p.status = com.smartdental.enums.PriceStatus.ACTIVE")
    boolean existsActiveByServiceId(@Param("serviceId") Long serviceId);

    @Query("select p from ServicePrice p where p.dentalService.id = :serviceId " +
            "and p.status = com.smartdental.enums.PriceStatus.ACTIVE " +
            "and p.effectiveFrom <= :date and (p.effectiveTo is null or p.effectiveTo >= :date)")
    Optional<ServicePrice> findCurrentPrice(@Param("serviceId") Long serviceId, @Param("date") LocalDate date);

    long countByDentalServiceId(Long serviceId);
}
