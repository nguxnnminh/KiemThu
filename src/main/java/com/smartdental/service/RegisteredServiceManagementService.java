package com.smartdental.service;

import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.RegisteredService;
import com.smartdental.entity.ServicePrice;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.RegisteredServiceStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.RegisteredServiceRepository;
import com.smartdental.repository.ServicePriceRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dang ky dich vu dieu tri cho mot phien kham (UC3.4).
 */
@Service
@RequiredArgsConstructor
public class RegisteredServiceManagementService {

    private final RegisteredServiceRepository registeredServiceRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<RegisteredService> findBySession(Long sessionId) {
        return registeredServiceRepository.findBySessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public List<DentalService> findActiveServices() {
        return dentalServiceRepository.findByStatus(CommonStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> findCurrentPricesByService(List<DentalService> dentalServices) {
        Map<Long, BigDecimal> prices = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (DentalService dentalService : dentalServices) {
            servicePriceRepository.findCurrentPrice(dentalService.getId(), today)
                    .ifPresent(price -> prices.put(dentalService.getId(), price.getPrice()));
        }
        return prices;
    }

    @Transactional
    public RegisteredService register(RegisteredServiceForm form) {
        if (form.getSessionId() == null || form.getServiceId() == null) {
            throw new BusinessException("Thieu thong tin phien kham hoac dich vu.");
        }
        if (form.getQuantity() == null || form.getQuantity() < 1) {
            throw new BusinessException("So luong phai lon hon 0.");
        }
        BigDecimal discount = form.getDiscountAmount() != null ? form.getDiscountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("So tien giam gia khong hop le.");
        }

        TreatmentSession session = treatmentSessionRepository.findById(form.getSessionId())
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
        if (session.getStatus() != TreatmentSessionStatus.OPEN) {
            throw new BusinessException("Phien kham da ket thuc, khong the dang ky dich vu.");
        }

        DentalService dentalService = dentalServiceRepository.findById(form.getServiceId())
                .orElseThrow(() -> new BusinessException("Khong tim thay dich vu."));
        if (dentalService.getStatus() != CommonStatus.ACTIVE) {
            throw new BusinessException("Dich vu khong con duoc cung cap.");
        }

        ServicePrice servicePrice = servicePriceRepository.findCurrentPrice(dentalService.getId(), LocalDate.now())
                .orElseThrow(() -> new BusinessException("Dich vu chua co bang gia hieu luc."));

        BigDecimal unitPrice = servicePrice.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(form.getQuantity()));
        if (discount.compareTo(subtotal) > 0) {
            throw new BusinessException("So tien giam gia khong duoc lon hon thanh tien.");
        }

        RegisteredService registeredService = new RegisteredService();
        registeredService.setTreatmentSession(session);
        registeredService.setPatient(session.getPatient());
        registeredService.setDentalService(dentalService);
        registeredService.setServicePrice(servicePrice);
        registeredService.setServiceNameSnapshot(dentalService.getName());
        registeredService.setUnitSnapshot(dentalService.getUnit());
        registeredService.setQuantity(form.getQuantity());
        registeredService.setUnitPrice(unitPrice);
        registeredService.setDiscountAmount(discount);
        registeredService.setTotalAmount(subtotal.subtract(discount));
        registeredService.setToothNumber(form.getToothNumber());
        registeredService.setStatus(RegisteredServiceStatus.ACTIVE);
        registeredService.setNote(form.getNote());
        registeredServiceRepository.save(registeredService);

        auditLogService.log("REGISTER_SERVICE", "RegisteredService", String.valueOf(registeredService.getId()),
                "Dang ky dich vu " + dentalService.getName() + " cho benh nhan " + session.getPatient().getFullName());
        return registeredService;
    }

    @Transactional
    public void cancel(Long registeredServiceId) {
        RegisteredService registeredService = registeredServiceRepository.findById(registeredServiceId)
                .orElseThrow(() -> new BusinessException("Khong tim thay dich vu da dang ky."));
        if (registeredService.getStatus() != RegisteredServiceStatus.ACTIVE) {
            throw new BusinessException("Chi co the huy dich vu dang hieu luc.");
        }
        if (registeredService.getTreatmentSession().getStatus() != TreatmentSessionStatus.OPEN) {
            throw new BusinessException("Phien kham da ket thuc, khong the huy dich vu.");
        }

        registeredService.setStatus(RegisteredServiceStatus.CANCELLED);
        registeredServiceRepository.save(registeredService);

        auditLogService.log("CANCEL_REGISTERED_SERVICE", "RegisteredService", String.valueOf(registeredService.getId()),
                "Huy dich vu " + registeredService.getServiceNameSnapshot()
                        + " cua benh nhan " + registeredService.getPatient().getFullName());
    }
}
