package com.smartdental.repository;

import com.smartdental.entity.ShiftCoefficient;
import com.smartdental.enums.ShiftCoefficientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftCoefficientRepository extends JpaRepository<ShiftCoefficient, Long> {

    @Query("select c from ShiftCoefficient c join fetch c.workShift order by c.effectiveFrom desc")
    List<ShiftCoefficient> findAllWithShift();

    @Query("select c from ShiftCoefficient c where c.workShift.id = :workShiftId " +
            "and c.status = com.smartdental.enums.ShiftCoefficientStatus.ACTIVE " +
            "and c.effectiveFrom <= :date and (c.effectiveTo is null or c.effectiveTo >= :date)")
    Optional<ShiftCoefficient> findActiveByShiftOnDate(@Param("workShiftId") Long workShiftId, @Param("date") LocalDate date);

    List<ShiftCoefficient> findByStatus(ShiftCoefficientStatus status);
}
