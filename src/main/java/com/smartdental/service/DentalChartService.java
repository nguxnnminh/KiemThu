package com.smartdental.service;

import com.smartdental.dto.form.ToothUpdateForm;
import com.smartdental.entity.DentalToothStatus;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.ToothTreatmentHistory;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.enums.ToothStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.DentalToothStatusRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.ToothTreatmentHistoryRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * So do rang FDI 32 va lich su dieu tri rang (UC3.3).
 */
@Service
@RequiredArgsConstructor
public class DentalChartService {

    private final DentalToothStatusRepository dentalToothStatusRepository;
    private final ToothTreatmentHistoryRepository toothTreatmentHistoryRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Map<Integer, DentalToothStatus> getChart(Long patientId) {
        Map<Integer, DentalToothStatus> chart = new HashMap<>();
        for (DentalToothStatus status : dentalToothStatusRepository.findByPatientId(patientId)) {
            chart.put(status.getToothNumber(), status);
        }
        return chart;
    }

    @Transactional(readOnly = true)
    public List<ToothTreatmentHistory> getHistory(Long patientId, Integer toothNumber) {
        return toothTreatmentHistoryRepository.findByPatientIdAndToothNumberOrderByUpdatedAtDesc(patientId, toothNumber);
    }

    @Transactional
    public void updateTooth(ToothUpdateForm form) {
        if (form.getPatientId() == null || form.getToothNumber() == null) {
            throw new BusinessException("Thieu thong tin benh nhan hoac so rang.");
        }
        if (form.getToothNumber() < 11 || form.getToothNumber() > 48) {
            throw new BusinessException("So rang khong hop le.");
        }
        ToothStatus newStatus;
        try {
            newStatus = ToothStatus.valueOf(form.getStatus());
        } catch (Exception ex) {
            throw new BusinessException("Trang thai rang khong hop le.");
        }

        Patient patient = patientRepository.findById(form.getPatientId())
                .orElseThrow(() -> new BusinessException("Khong tim thay benh nhan."));
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseThrow(() -> new BusinessException("Benh nhan chua co ho so benh an."));

        TreatmentSession session = null;
        if (form.getSessionId() != null) {
            session = treatmentSessionRepository.findById(form.getSessionId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
            if (session.getStatus() != TreatmentSessionStatus.OPEN) {
                throw new BusinessException("Phien kham da ket thuc, khong the cap nhat so do rang.");
            }
        }

        DentalToothStatus toothStatus = dentalToothStatusRepository
                .findByPatientIdAndToothNumber(patient.getId(), form.getToothNumber())
                .orElseGet(() -> {
                    DentalToothStatus newToothStatus = new DentalToothStatus();
                    newToothStatus.setPatient(patient);
                    newToothStatus.setMedicalRecord(medicalRecord);
                    newToothStatus.setToothNumber(form.getToothNumber());
                    newToothStatus.setStatus(ToothStatus.NORMAL);
                    return newToothStatus;
                });

        ToothStatus oldStatus = toothStatus.getStatus();

        toothStatus.setStatus(newStatus);
        toothStatus.setDiagnosis(form.getDiagnosis());
        toothStatus.setNote(form.getNote());
        if (session != null) {
            toothStatus.setLastUpdatedSession(session);
        }
        dentalToothStatusRepository.save(toothStatus);

        ToothTreatmentHistory history = new ToothTreatmentHistory();
        history.setMedicalRecord(medicalRecord);
        history.setPatient(patient);
        if (session != null) {
            history.setTreatmentSession(session);
        } else {
            throw new BusinessException("Phai chon phien kham de cap nhat trang thai rang.");
        }
        history.setToothNumber(form.getToothNumber());
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setDiagnosis(form.getDiagnosis());
        history.setNote(form.getNote());
        history.setUpdatedBy(session.getDoctor() != null ? session.getDoctor().getFullName() : null);
        toothTreatmentHistoryRepository.save(history);

        auditLogService.log("UPDATE_TOOTH_STATUS", "DentalToothStatus", patient.getPatientCode(),
                "Cap nhat rang so " + form.getToothNumber() + " cho benh nhan " + patient.getFullName()
                        + " sang trang thai " + newStatus.getLabel());
    }
}
