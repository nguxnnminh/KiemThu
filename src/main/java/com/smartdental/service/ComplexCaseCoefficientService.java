package com.smartdental.service;

import com.smartdental.dto.form.ComplexCaseCoefficientForm;
import com.smartdental.dto.form.ComplexCaseDecisionForm;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.Employee;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.ComplexCaseCoefficientRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * UC4.3 - Nhap he so ca phuc tap trong thang.
 */
@Service
@RequiredArgsConstructor
public class ComplexCaseCoefficientService {

    private static final BigDecimal MIN_COEFFICIENT = new BigDecimal("0.1");
    private static final BigDecimal MAX_COEFFICIENT = new BigDecimal("0.5");

    private final ComplexCaseCoefficientRepository complexCaseCoefficientRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final AuditLogService auditLogService;
    private final CodeGeneratorService codeGeneratorService;

    @Transactional(readOnly = true)
    public Page<ComplexCaseCoefficient> search(ApprovalStatus status, Pageable pageable) {
        return complexCaseCoefficientRepository.search(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<ComplexCaseCoefficient> findByDoctorId(Long doctorId) {
        return complexCaseCoefficientRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public ComplexCaseCoefficient getById(Long id) {
        return complexCaseCoefficientRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay he so ca phuc tap."));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ComplexCaseCoefficient> findByTreatmentSessionId(Long sessionId) {
        return complexCaseCoefficientRepository.findByTreatmentSessionId(sessionId);
    }

    /**
     * Bac si de xuat he so ca phuc tap cho phien dieu tri da hoan tat cua chinh minh.
     */
    @Transactional
    public ComplexCaseCoefficient propose(ComplexCaseCoefficientForm form, Employee doctor) {
        if (form.getTreatmentSessionId() == null) {
            throw new BusinessException("Vui long chon phien dieu tri.");
        }
        if (form.getCoefficient() == null
                || form.getCoefficient().compareTo(MIN_COEFFICIENT) < 0
                || form.getCoefficient().compareTo(MAX_COEFFICIENT) > 0) {
            throw new BusinessException("He so ca phuc tap phai trong khoang tu 0.1 den 0.5.");
        }

        TreatmentSession session = treatmentSessionRepository.findById(form.getTreatmentSessionId())
                .orElseThrow(() -> new BusinessException("Khong tim thay phien dieu tri."));

        if (session.getStatus() != TreatmentSessionStatus.COMPLETED) {
            throw new BusinessException("Chi co the de xuat he so cho phien dieu tri da hoan tat.");
        }
        if (!session.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("Ban chi co the de xuat he so cho phien dieu tri cua chinh minh.");
        }
        if (complexCaseCoefficientRepository.existsByTreatmentSessionId(session.getId())) {
            throw new BusinessException("Phien dieu tri nay da co de xuat he so ca phuc tap.");
        }

        ComplexCaseCoefficient coefficient = new ComplexCaseCoefficient();
        coefficient.setCoefficientCode(codeGeneratorService.nextCode(CodePrefix.COMPLEX_CASE_COEFFICIENT));
        coefficient.setTreatmentSession(session);
        coefficient.setDoctor(doctor);
        coefficient.setCoefficient(form.getCoefficient());
        coefficient.setReason(form.getReason());
        coefficient.setStatus(ApprovalStatus.PENDING);
        coefficient.setProposedBy(currentUsername());
        complexCaseCoefficientRepository.save(coefficient);

        auditLogService.log("PROPOSE_COMPLEX_CASE_COEFFICIENT", "ComplexCaseCoefficient", coefficient.getCoefficientCode(),
                "De xuat he so ca phuc tap cho phien " + session.getSessionCode());
        return coefficient;
    }

    /**
     * De xuat he so ca phuc tap ngay tu man hinh kham, ngay sau khi hoan tat phien kham.
     * Duoc goi tu ExaminationService.completeExamination trong cung 1 transaction,
     * tai su dung toan bo validate cua propose().
     */
    @Transactional
    public ComplexCaseCoefficient proposeFromExam(Long sessionId, Employee doctor, BigDecimal coefficient, String reason) {
        ComplexCaseCoefficientForm form = new ComplexCaseCoefficientForm();
        form.setTreatmentSessionId(sessionId);
        form.setCoefficient(coefficient);
        form.setReason(reason);
        return propose(form, doctor);
    }

    @Transactional
    public ComplexCaseCoefficient approve(Long id) {
        ComplexCaseCoefficient coefficient = getById(id);
        if (coefficient.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("He so ca phuc tap nay da duoc xu ly.");
        }
        coefficient.setStatus(ApprovalStatus.APPROVED);
        coefficient.setApprovedBy(currentUsername());
        complexCaseCoefficientRepository.save(coefficient);

        auditLogService.log("APPROVE_COMPLEX_CASE_COEFFICIENT", "ComplexCaseCoefficient", coefficient.getCoefficientCode(),
                "Duyet he so ca phuc tap " + coefficient.getCoefficientCode());
        return coefficient;
    }

    @Transactional
    public ComplexCaseCoefficient reject(Long id, ComplexCaseDecisionForm form) {
        ComplexCaseCoefficient coefficient = getById(id);
        if (coefficient.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException("He so ca phuc tap nay da duoc xu ly.");
        }
        coefficient.setStatus(ApprovalStatus.REJECTED);
        coefficient.setApprovedBy(currentUsername());
        coefficient.setRejectReason(form.getRejectReason());
        complexCaseCoefficientRepository.save(coefficient);

        auditLogService.log("REJECT_COMPLEX_CASE_COEFFICIENT", "ComplexCaseCoefficient", coefficient.getCoefficientCode(),
                "Tu choi he so ca phuc tap " + coefficient.getCoefficientCode());
        return coefficient;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
