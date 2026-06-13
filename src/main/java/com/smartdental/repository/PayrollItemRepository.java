package com.smartdental.repository;

import com.smartdental.entity.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    @Query("select i from PayrollItem i " +
            "left join fetch i.doctorShiftRegistration r " +
            "left join fetch r.room left join fetch r.chair left join fetch r.workShift " +
            "left join fetch i.treatmentSession s left join fetch s.patient " +
            "where i.payrollSlip.id = :slipId order by i.workDate, i.id")
    List<PayrollItem> findBySlipId(@Param("slipId") Long slipId);

    void deleteByPayrollSlipId(Long slipId);
}
