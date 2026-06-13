package com.smartdental.repository;

import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ComplexCaseCoefficientRepository extends JpaRepository<ComplexCaseCoefficient, Long> {

    @Query("select c from ComplexCaseCoefficient c " +
            "join fetch c.treatmentSession s join fetch c.doctor " +
            "where (:status is null or c.status = :status) order by c.createdAt desc")
    Page<ComplexCaseCoefficient> search(@Param("status") ApprovalStatus status, Pageable pageable);

    @Query("select c from ComplexCaseCoefficient c " +
            "join fetch c.treatmentSession s join fetch c.doctor " +
            "where c.doctor.id = :doctorId order by c.createdAt desc")
    List<ComplexCaseCoefficient> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("select c from ComplexCaseCoefficient c " +
            "join fetch c.treatmentSession s join fetch c.doctor " +
            "where c.id = :id")
    java.util.Optional<ComplexCaseCoefficient> findDetailById(@Param("id") Long id);

    boolean existsByTreatmentSessionId(Long treatmentSessionId);

    @Query("select case when count(c) > 0 then true else false end from ComplexCaseCoefficient c " +
            "where c.doctor.id = :doctorId and c.status = com.smartdental.enums.ApprovalStatus.PENDING " +
            "and c.treatmentSession.examinationDate >= :fromDate and c.treatmentSession.examinationDate <= :toDate")
    boolean existsPendingForDoctorInPeriod(@Param("doctorId") Long doctorId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);

    @Query("select c from ComplexCaseCoefficient c where c.treatmentSession.id = :sessionId " +
            "and c.status = com.smartdental.enums.ApprovalStatus.APPROVED")
    List<ComplexCaseCoefficient> findApprovedBySessionId(@Param("sessionId") Long sessionId);

    @Query("select c from ComplexCaseCoefficient c join fetch c.doctor where c.treatmentSession.id = :sessionId")
    java.util.Optional<ComplexCaseCoefficient> findByTreatmentSessionId(@Param("sessionId") Long sessionId);
}
