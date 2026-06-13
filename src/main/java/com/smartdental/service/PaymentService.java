package com.smartdental.service;

import com.smartdental.dto.form.PaymentForm;
import com.smartdental.dto.form.RefundForm;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.Payment;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.PaymentMethod;
import com.smartdental.enums.PaymentStatus;
import com.smartdental.enums.PaymentType;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thu tien va hoan tien cho hoa don (UC3.5).
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Payment> findByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByPaidAtAsc(invoiceId);
    }

    @Transactional
    public Payment collect(PaymentForm form) {
        if (form.getInvoiceId() == null) {
            throw new BusinessException("Thieu thong tin hoa don.");
        }
        Invoice invoice = invoiceRepository.findById(form.getInvoiceId())
                .orElseThrow(() -> new BusinessException("Khong tim thay hoa don."));
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Hoa don da thanh toan du hoac da huy.");
        }

        BigDecimal amount = form.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("So tien thu phai lon hon 0.");
        }
        if (amount.compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BusinessException("So tien thu khong duoc lon hon so tien con lai.");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(form.getPaymentMethod());
        } catch (Exception ex) {
            throw new BusinessException("Phuong thuc thanh toan khong hop le.");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setCollectedBy(currentUsername());
        payment.setPaidAt(LocalDateTime.now());
        payment.setNote(form.getNote());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().add(amount));
        invoice.setRemainingAmount(invoice.getFinalAmount().subtract(invoice.getPaidAmount()));
        invoice.setStatus(invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0
                ? InvoiceStatus.PAID : InvoiceStatus.PARTIAL);
        invoiceRepository.save(invoice);

        auditLogService.log("COLLECT_PAYMENT", "Invoice", invoice.getInvoiceCode(),
                "Thu " + amount + " VND cho hoa don cua benh nhan " + invoice.getPatient().getFullName());
        return payment;
    }

    @Transactional
    public Payment refund(RefundForm form) {
        if (form.getInvoiceId() == null) {
            throw new BusinessException("Thieu thong tin hoa don.");
        }
        Invoice invoice = invoiceRepository.findById(form.getInvoiceId())
                .orElseThrow(() -> new BusinessException("Khong tim thay hoa don."));
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Hoa don chua co khoan thu nao de hoan tien.");
        }

        BigDecimal amount = form.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("So tien hoan phai lon hon 0.");
        }
        if (amount.compareTo(invoice.getPaidAmount()) > 0) {
            throw new BusinessException("So tien hoan khong duoc lon hon so tien da thu.");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(form.getPaymentMethod());
        } catch (Exception ex) {
            throw new BusinessException("Phuong thuc hoan tien khong hop le.");
        }

        Payment refund = new Payment();
        refund.setInvoice(invoice);
        refund.setAmount(amount);
        refund.setPaymentMethod(method);
        refund.setPaymentType(PaymentType.REFUND);
        refund.setCollectedBy(currentUsername());
        refund.setPaidAt(LocalDateTime.now());
        refund.setNote(form.getNote());
        refund.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(refund);

        invoice.setPaidAmount(invoice.getPaidAmount().subtract(amount));
        invoice.setRemainingAmount(invoice.getFinalAmount().subtract(invoice.getPaidAmount()));
        invoice.setStatus(invoice.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0
                ? InvoiceStatus.UNPAID : InvoiceStatus.PARTIAL);
        invoiceRepository.save(invoice);

        auditLogService.log("REFUND_PAYMENT", "Invoice", invoice.getInvoiceCode(),
                "Hoan " + amount + " VND cho hoa don cua benh nhan " + invoice.getPatient().getFullName());
        return refund;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
