package com.smartdental.service;

import com.smartdental.dto.form.PatientForm;
import com.smartdental.entity.Patient;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Nghiep vu quan ly ho so benh nhan (UC2.6).
 */
@Service
@RequiredArgsConstructor
public class PatientManagementService {

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^0\\d{9}$");

    private final PatientRepository patientRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Patient> search(String keyword, CommonStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return patientRepository.search(kw, status, pageable);
    }

    /** So lieu tong hop hien thi tren stat-row. */
    public record PatientStats(long total, long active, long inactive) {
    }

    @Transactional(readOnly = true)
    public PatientStats stats() {
        long total = patientRepository.count();
        long active = patientRepository.countByStatus(CommonStatus.ACTIVE);
        return new PatientStats(total, active, total - active);
    }

    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay benh nhan."));
    }

    @Transactional
    public Patient create(PatientForm form) {
        validate(form, null);

        Patient patient = new Patient();
        patient.setPatientCode(codeGeneratorService.nextCode(CodePrefix.PATIENT));
        applyForm(patient, form);

        patientRepository.save(patient);
        auditLogService.log("CREATE_PATIENT", "Patient", patient.getPatientCode(),
                "Tao ho so benh nhan " + patient.getFullName());
        return patient;
    }

    @Transactional
    public Patient update(PatientForm form) {
        Patient patient = getById(form.getId());
        validate(form, patient);

        applyForm(patient, form);
        patientRepository.save(patient);
        auditLogService.log("UPDATE_PATIENT", "Patient", patient.getPatientCode(),
                "Cap nhat ho so benh nhan " + patient.getFullName());
        return patient;
    }

    @Transactional
    public void deactivate(Long id) {
        Patient patient = getById(id);
        patient.setStatus(CommonStatus.INACTIVE);
        patientRepository.save(patient);
        auditLogService.log("DEACTIVATE_PATIENT", "Patient", patient.getPatientCode(),
                "Ngung hoat dong ho so benh nhan " + patient.getFullName());
    }

    @Transactional
    public void activate(Long id) {
        Patient patient = getById(id);
        patient.setStatus(CommonStatus.ACTIVE);
        patientRepository.save(patient);
        auditLogService.log("ACTIVATE_PATIENT", "Patient", patient.getPatientCode(),
                "Kich hoat lai ho so benh nhan " + patient.getFullName());
    }

    private void applyForm(Patient patient, PatientForm form) {
        patient.setFullName(form.getFullName().trim());
        patient.setDateOfBirth(form.getDateOfBirth());
        patient.setGender(form.getGender());
        patient.setPhone(form.getPhone());
        patient.setEmail(form.getEmail());
        patient.setAddress(form.getAddress());
        if (form.getStatus() != null) {
            patient.setStatus(form.getStatus());
        }
    }

    private void validate(PatientForm form, Patient existing) {
        if (form.getFullName() == null || form.getFullName().isBlank()) {
            throw new BusinessException("Vui long nhap ho ten benh nhan.");
        }
        if (form.getPhone() == null || !PHONE_PATTERN.matcher(form.getPhone()).matches()) {
            throw new BusinessException("So dien thoai phai gom 10 chu so va bat dau bang so 0.");
        }

        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (patientRepository.existsByPhone(form.getPhone())) {
                throw new BusinessException("So dien thoai da duoc su dung boi benh nhan khac.");
            }
        } else if (patientRepository.existsByPhoneAndIdNot(form.getPhone(), currentId)) {
            throw new BusinessException("So dien thoai da duoc su dung boi benh nhan khac.");
        }
    }
}
