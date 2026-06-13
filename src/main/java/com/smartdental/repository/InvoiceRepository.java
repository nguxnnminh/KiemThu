package com.smartdental.repository;

import com.smartdental.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    Optional<Invoice> findByTreatmentSessionId(Long treatmentSessionId);

    @Query("select i from Invoice i " +
            "join fetch i.patient " +
            "join fetch i.treatmentSession s join fetch s.doctor " +
            "where i.id = :id")
    Optional<Invoice> findDetailById(@Param("id") Long id);

    long countByStatus(com.smartdental.enums.InvoiceStatus status);

    @Query("select coalesce(sum(i.paidAmount), 0) from Invoice i where i.status <> com.smartdental.enums.InvoiceStatus.CANCELLED")
    java.math.BigDecimal sumPaidAmount();

    @Query("select coalesce(sum(i.finalAmount - i.paidAmount), 0) from Invoice i " +
            "where i.status = com.smartdental.enums.InvoiceStatus.UNPAID or i.status = com.smartdental.enums.InvoiceStatus.PARTIAL")
    java.math.BigDecimal sumOutstandingAmount();

    @Query("select i from Invoice i join fetch i.treatmentSession s " +
            "where i.patient.id = :patientId order by i.createdAt desc")
    Page<Invoice> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    @Query("select i from Invoice i " +
            "join fetch i.patient join fetch i.treatmentSession s where " +
            "(:keyword is null or lower(i.invoiceCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(i.patient.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(i.patient.phone) like lower(concat('%', :keyword, '%'))) " +
            "and (:status is null or i.status = :status) " +
            "and (:fromDate is null or s.examinationDate >= :fromDate) " +
            "and (:toDate is null or s.examinationDate <= :toDate) " +
            "order by i.createdAt desc")
    Page<Invoice> search(@Param("keyword") String keyword,
                          @Param("status") com.smartdental.enums.InvoiceStatus status,
                          @Param("fromDate") LocalDate fromDate,
                          @Param("toDate") LocalDate toDate,
                          Pageable pageable);
}
