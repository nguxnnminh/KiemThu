package com.smartdental.repository;

import com.smartdental.entity.RegisteredService;
import com.smartdental.enums.RegisteredServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RegisteredServiceRepository extends JpaRepository<RegisteredService, Long> {

    @Query("select r from RegisteredService r join fetch r.dentalService " +
            "where r.treatmentSession.id = :sessionId and r.status <> com.smartdental.enums.RegisteredServiceStatus.CANCELLED " +
            "order by r.id asc")
    List<RegisteredService> findBySessionId(@Param("sessionId") Long sessionId);

    List<RegisteredService> findByTreatmentSessionIdAndStatus(Long treatmentSessionId, RegisteredServiceStatus status);

    @Query("select r from RegisteredService r join r.treatmentSession s " +
            "where r.status = com.smartdental.enums.RegisteredServiceStatus.INVOICED " +
            "and (:fromDate is null or s.examinationDate >= :fromDate) " +
            "and (:toDate is null or s.examinationDate <= :toDate)")
    List<RegisteredService> findInvoicedForReport(@Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate);
}
