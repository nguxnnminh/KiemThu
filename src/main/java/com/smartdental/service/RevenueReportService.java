package com.smartdental.service;

import com.smartdental.dto.RevenueSummary;
import com.smartdental.entity.Payment;
import com.smartdental.enums.PaymentMethod;
import com.smartdental.enums.PaymentType;
import com.smartdental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bao cao doanh thu theo khoang thoi gian, phuong thuc thanh toan, theo ngay (UC3.6).
 */
@Service
@RequiredArgsConstructor
public class RevenueReportService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<Payment> findPayments(LocalDate fromDate, LocalDate toDate, PaymentMethod method) {
        return paymentRepository.findForRevenueReport(fromDate, toDate, method);
    }

    @Transactional(readOnly = true)
    public RevenueSummary summarize(LocalDate fromDate, LocalDate toDate, PaymentMethod method) {
        List<Payment> payments = findPayments(fromDate, toDate, method);
        BigDecimal totalPayments = sum(payments, PaymentType.PAYMENT);
        BigDecimal totalRefunds = sum(payments, PaymentType.REFUND);
        return new RevenueSummary("Tổng cộng", totalPayments, totalRefunds, totalPayments.subtract(totalRefunds));
    }

    @Transactional(readOnly = true)
    public List<RevenueSummary> summarizeByMethod(LocalDate fromDate, LocalDate toDate) {
        List<Payment> payments = findPayments(fromDate, toDate, null);
        List<RevenueSummary> result = new java.util.ArrayList<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            List<Payment> filtered = payments.stream().filter(p -> p.getPaymentMethod() == method).toList();
            BigDecimal totalPayments = sum(filtered, PaymentType.PAYMENT);
            BigDecimal totalRefunds = sum(filtered, PaymentType.REFUND);
            result.add(new RevenueSummary(method.getLabel(), totalPayments, totalRefunds, totalPayments.subtract(totalRefunds)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<RevenueSummary> summarizeByDay(LocalDate fromDate, LocalDate toDate, PaymentMethod method) {
        List<Payment> payments = findPayments(fromDate, toDate, method);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Map<LocalDate, List<Payment>> byDay = new LinkedHashMap<>();
        for (Payment payment : payments) {
            byDay.computeIfAbsent(payment.getPaidAt().toLocalDate(), k -> new java.util.ArrayList<>()).add(payment);
        }

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    BigDecimal totalPayments = sum(entry.getValue(), PaymentType.PAYMENT);
                    BigDecimal totalRefunds = sum(entry.getValue(), PaymentType.REFUND);
                    return new RevenueSummary(entry.getKey().format(formatter), totalPayments, totalRefunds,
                            totalPayments.subtract(totalRefunds));
                })
                .toList();
    }

    private BigDecimal sum(List<Payment> payments, PaymentType type) {
        return payments.stream()
                .filter(p -> p.getPaymentType() == type)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
