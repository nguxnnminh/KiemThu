package com.smartdental.service;

import com.smartdental.dto.form.ServicePriceForm;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.ServicePrice;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.PriceStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.ServicePriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Nghiep vu quan ly bang gia dich vu (UC1.4).
 */
@Service
@RequiredArgsConstructor
public class ServicePriceManagementService {

    private final ServicePriceRepository servicePriceRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Optional<ServicePrice> findCurrentPrice(Long serviceId, LocalDate date) {
        return servicePriceRepository.findCurrentPrice(serviceId, date);
    }

    @Transactional(readOnly = true)
    public Optional<ServicePrice> findActivePrice(Long serviceId) {
        return servicePriceRepository.findByDentalServiceIdAndStatus(serviceId, PriceStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentPrice(Long serviceId) {
        return servicePriceRepository.existsActiveByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public long countActiveServicesWithoutCurrentPrice() {
        return dentalServiceRepository.findByStatus(CommonStatus.ACTIVE).stream()
                .filter(s -> !servicePriceRepository.existsActiveByServiceId(s.getId()))
                .count();
    }

    @Transactional(readOnly = true)
    public List<ServicePrice> history(Long serviceId) {
        return servicePriceRepository.findByDentalServiceIdOrderByEffectiveFromDesc(serviceId);
    }

    @Transactional(readOnly = true)
    public boolean matchesPriceFilter(Long serviceId, PriceStatus status, LocalDate fromDate, LocalDate toDate) {
        if (status == null && fromDate == null && toDate == null) {
            return true;
        }
        return history(serviceId).stream().anyMatch(p -> {
            if (status != null && p.getStatus() != status) {
                return false;
            }
            if (fromDate != null && (p.getEffectiveTo() != null && p.getEffectiveTo().isBefore(fromDate))) {
                return false;
            }
            if (toDate != null && p.getEffectiveFrom().isAfter(toDate)) {
                return false;
            }
            return true;
        });
    }

    @Transactional
    public ServicePrice createNewPrice(ServicePriceForm form, String currentUsername) {
        if (form.getServiceId() == null) {
            throw new BusinessException("Vui long chon dich vu.");
        }
        if (form.getPrice() == null || form.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Gia dich vu phai lon hon 0.");
        }
        if (form.getEffectiveFrom() == null) {
            throw new BusinessException("Vui long chon ngay hieu luc.");
        }
        if (form.getReason() == null || form.getReason().isBlank()) {
            throw new BusinessException("Vui long nhap ly do thay doi gia.");
        }

        DentalService dentalService = dentalServiceRepository.findById(form.getServiceId())
                .orElseThrow(() -> new BusinessException("Khong tim thay dich vu."));

        if (dentalService.getStatus() != CommonStatus.ACTIVE) {
            throw new BusinessException("Khong the thiet lap gia cho dich vu da ngung cung cap.");
        }

        Optional<ServicePrice> currentActive = servicePriceRepository.findByDentalServiceIdAndStatus(dentalService.getId(), PriceStatus.ACTIVE);

        if (currentActive.isPresent()) {
            ServicePrice current = currentActive.get();
            if (!form.getEffectiveFrom().isAfter(current.getEffectiveFrom())) {
                throw new BusinessException("Ngay hieu luc moi phai sau ngay hieu luc cua gia hien hanh.");
            }
            current.setEffectiveTo(form.getEffectiveFrom().minusDays(1));
            current.setStatus(PriceStatus.EXPIRED);
            servicePriceRepository.save(current);
            auditLogService.log("EXPIRE_SERVICE_PRICE", "ServicePrice", dentalService.getServiceCode(),
                    "Het hieu luc gia cu cua dich vu " + dentalService.getName());
        }

        ServicePrice newPrice = new ServicePrice();
        newPrice.setDentalService(dentalService);
        newPrice.setPrice(form.getPrice());
        newPrice.setEffectiveFrom(form.getEffectiveFrom());
        newPrice.setReason(form.getReason());
        newPrice.setStatus(PriceStatus.ACTIVE);
        newPrice.setCreatedBy(currentUsername);

        servicePriceRepository.save(newPrice);
        auditLogService.log("CREATE_SERVICE_PRICE", "ServicePrice", dentalService.getServiceCode(),
                "Thiet lap gia moi " + form.getPrice() + " cho dich vu " + dentalService.getName()
                        + " tu ngay " + form.getEffectiveFrom());
        return newPrice;
    }
}
