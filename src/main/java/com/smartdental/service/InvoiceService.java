package com.smartdental.service;

import com.smartdental.dto.form.InvoiceDiscountForm;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.Patient;
import com.smartdental.entity.RegisteredService;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.DiscountType;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.RegisteredServiceStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.RegisteredServiceRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Lap hoa don, ap dung giam gia cho hoa don (UC3.5).
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final RegisteredServiceRepository registeredServiceRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Invoice> search(String keyword, InvoiceStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return invoiceRepository.search(kw, status, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findByPatient(Long patientId, Pageable pageable) {
        return invoiceRepository.findByPatientId(patientId, pageable);
    }

    /** So lieu tong hop hien thi tren stat-row. */
    public record InvoiceStats(long total, long paid, long unpaid,
                               java.math.BigDecimal totalPaid, java.math.BigDecimal outstanding) {
    }

    @Transactional(readOnly = true)
    public InvoiceStats stats() {
        return new InvoiceStats(
                invoiceRepository.count(),
                invoiceRepository.countByStatus(com.smartdental.enums.InvoiceStatus.PAID),
                invoiceRepository.countByStatus(com.smartdental.enums.InvoiceStatus.UNPAID),
                invoiceRepository.sumPaidAmount(),
                invoiceRepository.sumOutstandingAmount());
    }

    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay hoa don."));
    }

    @Transactional(readOnly = true)
    public List<RegisteredService> getInvoiceItems(Long sessionId) {
        return registeredServiceRepository.findByTreatmentSessionIdAndStatus(sessionId, RegisteredServiceStatus.INVOICED);
    }

    /**
     * Tu dong lap hoa don tu cac dich vu da dang ky (ACTIVE) cua phien kham da hoan tat,
     * duoc goi tu ExaminationService.completeExamination trong cung 1 transaction.
     * - Neu phien kham da co hoa don, tra ve hoa don hien co (idempotent).
     * - Neu khong co dich vu ACTIVE nao, tra ve null (khong loi).
     */
    @Transactional
    public Invoice createInvoiceIfEligible(Long sessionId) {
        TreatmentSession session = treatmentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));

        return invoiceRepository.findByTreatmentSessionId(sessionId)
                .orElseGet(() -> {
                    List<RegisteredService> services = registeredServiceRepository.findByTreatmentSessionIdAndStatus(
                            sessionId, RegisteredServiceStatus.ACTIVE);
                    if (services.isEmpty()) {
                        return null;
                    }
                    return buildAndSaveInvoice(session, services);
                });
    }

    private Invoice buildAndSaveInvoice(TreatmentSession session, List<RegisteredService> services) {
        BigDecimal total = services.stream()
                .map(RegisteredService::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Patient patient = session.getPatient();

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(codeGeneratorService.nextCode(CodePrefix.INVOICE));
        invoice.setPatient(patient);
        invoice.setTreatmentSession(session);
        invoice.setTotalAmount(total);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setDiscountType(DiscountType.NONE);
        invoice.setFinalAmount(total);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setRemainingAmount(total);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setCreatedBy(currentUsername());
        invoiceRepository.save(invoice);

        for (RegisteredService service : services) {
            service.setStatus(RegisteredServiceStatus.INVOICED);
            registeredServiceRepository.save(service);
        }

        auditLogService.log("CREATE_INVOICE", "Invoice", invoice.getInvoiceCode(),
                "Lap hoa don cho benh nhan " + patient.getFullName());
        return invoice;
    }

    @Transactional
    public Invoice applyDiscount(InvoiceDiscountForm form) {
        if (form.getInvoiceId() == null) {
            throw new BusinessException("Thieu thong tin hoa don.");
        }
        Invoice invoice = getById(form.getInvoiceId());
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Khong the chinh sua giam gia cua hoa don da thanh toan hoac da huy.");
        }
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Khong the chinh sua giam gia sau khi da thu tien.");
        }

        DiscountType discountType;
        try {
            discountType = DiscountType.valueOf(form.getDiscountType());
        } catch (Exception ex) {
            throw new BusinessException("Hinh thuc giam gia khong hop le.");
        }

        BigDecimal discountAmount;
        BigDecimal value = form.getDiscountValue() != null ? form.getDiscountValue() : BigDecimal.ZERO;
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Gia tri giam gia khong hop le.");
        }

        switch (discountType) {
            case NONE -> discountAmount = BigDecimal.ZERO;
            case AMOUNT -> discountAmount = value;
            case PERCENT -> {
                if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new BusinessException("Phan tram giam gia khong duoc vuot qua 100%.");
                }
                discountAmount = invoice.getTotalAmount().multiply(value)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            default -> discountAmount = BigDecimal.ZERO;
        }

        if (discountAmount.compareTo(invoice.getTotalAmount()) > 0) {
            throw new BusinessException("So tien giam gia khong duoc lon hon tong tien hoa don.");
        }

        invoice.setDiscountType(discountType);
        invoice.setDiscountAmount(discountAmount);
        invoice.setDiscountNote(form.getDiscountNote());
        invoice.setFinalAmount(invoice.getTotalAmount().subtract(discountAmount));
        invoice.setRemainingAmount(invoice.getFinalAmount().subtract(invoice.getPaidAmount()));
        invoiceRepository.save(invoice);

        auditLogService.log("APPLY_INVOICE_DISCOUNT", "Invoice", invoice.getInvoiceCode(),
                "Ap dung giam gia cho hoa don cua benh nhan " + invoice.getPatient().getFullName());
        return invoice;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
