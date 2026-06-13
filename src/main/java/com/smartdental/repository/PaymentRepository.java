package com.smartdental.repository;

import com.smartdental.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceIdOrderByPaidAtAsc(Long invoiceId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p " +
            "where p.invoice.id = :invoiceId and p.status = com.smartdental.enums.PaymentStatus.SUCCESS " +
            "and p.paymentType = com.smartdental.enums.PaymentType.PAYMENT")
    java.math.BigDecimal sumPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p " +
            "where p.invoice.id = :invoiceId and p.status = com.smartdental.enums.PaymentStatus.SUCCESS " +
            "and p.paymentType = com.smartdental.enums.PaymentType.REFUND")
    java.math.BigDecimal sumRefundsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("select p from Payment p join fetch p.invoice i join fetch i.patient " +
            "where p.status = com.smartdental.enums.PaymentStatus.SUCCESS " +
            "and (:fromDate is null or cast(p.paidAt as date) >= :fromDate) " +
            "and (:toDate is null or cast(p.paidAt as date) <= :toDate) " +
            "and (:paymentMethod is null or p.paymentMethod = :paymentMethod) " +
            "order by p.paidAt desc")
    List<Payment> findForRevenueReport(@Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate,
                                        @Param("paymentMethod") com.smartdental.enums.PaymentMethod paymentMethod);
}
