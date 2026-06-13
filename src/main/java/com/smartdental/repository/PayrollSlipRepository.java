package com.smartdental.repository;

import com.smartdental.entity.PayrollSlip;
import com.smartdental.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollSlipRepository extends JpaRepository<PayrollSlip, Long> {

    @Query("select p from PayrollSlip p join fetch p.doctor " +
            "where (:year is null or p.payrollYear = :year) " +
            "and (:month is null or p.payrollMonth = :month) " +
            "and (:doctorId is null or p.doctor.id = :doctorId) " +
            "order by p.payrollYear desc, p.payrollMonth desc")
    Page<PayrollSlip> search(@Param("year") Integer year,
                              @Param("month") Integer month,
                              @Param("doctorId") Long doctorId,
                              Pageable pageable);

    @Query("select p from PayrollSlip p join fetch p.doctor where p.id = :id")
    Optional<PayrollSlip> findDetailById(@Param("id") Long id);

    Optional<PayrollSlip> findByDoctorIdAndPayrollYearAndPayrollMonth(Long doctorId, Integer year, Integer month);

    @Query("select p from PayrollSlip p join fetch p.doctor " +
            "where p.payrollYear = :year and p.payrollMonth = :month order by p.doctor.fullName")
    List<PayrollSlip> findByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("select p from PayrollSlip p where p.doctor.id = :doctorId and p.payrollYear = :year " +
            "and p.status = com.smartdental.enums.PayrollStatus.APPROVED order by p.payrollMonth")
    List<PayrollSlip> findApprovedByDoctorAndYear(@Param("doctorId") Long doctorId, @Param("year") Integer year);

    @Query("select p from PayrollSlip p join fetch p.doctor where p.payrollYear = :year " +
            "and p.status = com.smartdental.enums.PayrollStatus.APPROVED order by p.doctor.fullName, p.payrollMonth")
    List<PayrollSlip> findApprovedByYear(@Param("year") Integer year);

    boolean existsByStatus(PayrollStatus status);
}
