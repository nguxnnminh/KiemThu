package com.smartdental.service;

import com.smartdental.dto.form.ExaminationForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.VisitCheckinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Kham benh: hang doi kham va ghi nhan ket qua kham (UC3.2).
 */
@Service
@RequiredArgsConstructor
public class ExaminationService {

    private static final java.math.BigDecimal MIN_COMPLEX_CASE_COEFFICIENT = new java.math.BigDecimal("0.1");
    private static final java.math.BigDecimal MAX_COMPLEX_CASE_COEFFICIENT = new java.math.BigDecimal("0.5");

    private final VisitCheckinRepository visitCheckinRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AppointmentStatusLogService appointmentStatusLogService;
    private final AuditLogService auditLogService;
    private final InvoiceService invoiceService;
    private final ComplexCaseCoefficientService complexCaseCoefficientService;
    private final AppointmentService appointmentService;

    @Transactional(readOnly = true)
    public List<VisitCheckin> findQueue(LocalDate date, Long doctorId) {
        return visitCheckinRepository.findByQueueDateAndStatus(date, CheckinStatus.WAITING, doctorId);
    }

    @Transactional(readOnly = true)
    public List<VisitCheckin> findInExamQueue(LocalDate date, Long doctorId) {
        return visitCheckinRepository.findByQueueDateAndStatus(date, CheckinStatus.IN_EXAM, doctorId);
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, Long> mapSessionIdsByAppointment(List<VisitCheckin> checkins) {
        java.util.Map<Long, Long> result = new java.util.HashMap<>();
        for (VisitCheckin checkin : checkins) {
            treatmentSessionRepository.findByAppointmentId(checkin.getAppointment().getId())
                    .ifPresent(session -> result.put(checkin.getId(), session.getId()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public TreatmentSession getSessionDetail(Long sessionId) {
        return treatmentSessionRepository.findDetailById(sessionId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
    }

    /**
     * Bat dau kham: tao MedicalRecord (neu chua co), tao TreatmentSession,
     * chuyen check-in sang IN_EXAM va lich hen sang IN_PROGRESS.
     */
    @Transactional
    public TreatmentSession startExamination(Long checkinId) {
        VisitCheckin checkin = visitCheckinRepository.findById(checkinId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phieu check-in."));

        if (checkin.getStatus() != CheckinStatus.WAITING) {
            throw new BusinessException("Chi co the bat dau kham cho benh nhan dang cho.");
        }

        Appointment appointment = checkin.getAppointment();
        if (appointment.getDoctor() == null) {
            throw new BusinessException("Lich hen chua duoc gan bac si.");
        }
        if (treatmentSessionRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new BusinessException("Lich hen nay da co phien kham.");
        }
        if (treatmentSessionRepository.existsByDoctorIdAndStatus(
                appointment.getDoctor().getId(), TreatmentSessionStatus.OPEN)) {
            throw new BusinessException("Bac si nay dang co benh nhan dang kham. Vui long hoan tat phien kham hien tai truoc.");
        }

        Patient patient = checkin.getPatient();
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord record = new MedicalRecord();
                    record.setRecordCode(codeGeneratorService.nextCode(CodePrefix.MEDICAL_RECORD));
                    record.setPatient(patient);
                    record.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(record);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode(codeGeneratorService.nextCode(CodePrefix.TREATMENT_SESSION));
        session.setAppointment(appointment);
        session.setMedicalRecord(medicalRecord);
        session.setPatient(patient);
        session.setDoctor(appointment.getDoctor());
        session.setCheckin(checkin);
        session.setExaminationDate(LocalDate.now());
        session.setStatus(TreatmentSessionStatus.OPEN);
        treatmentSessionRepository.save(session);

        checkin.setStatus(CheckinStatus.IN_EXAM);
        visitCheckinRepository.save(checkin);

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);
        appointmentStatusLogService.log(appointment, oldStatus, AppointmentStatus.IN_PROGRESS, "Bat dau kham benh");

        auditLogService.log("START_EXAMINATION", "TreatmentSession", session.getSessionCode(),
                "Bat dau kham benh nhan " + patient.getFullName());
        return session;
    }

    @Transactional
    public TreatmentSession saveExamination(ExaminationForm form) {
        if (form.getSessionId() == null) {
            throw new BusinessException("Khong xac dinh duoc phien kham.");
        }
        TreatmentSession session = treatmentSessionRepository.findById(form.getSessionId())
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
        if (session.getStatus() != TreatmentSessionStatus.OPEN) {
            throw new BusinessException("Phien kham da ket thuc, khong the cap nhat.");
        }

        session.setSymptom(form.getSymptom());
        session.setDiagnosis(form.getDiagnosis());
        session.setTreatmentPlan(form.getTreatmentPlan());
        session.setDoctorNote(form.getDoctorNote());
        treatmentSessionRepository.save(session);

        auditLogService.log("UPDATE_EXAMINATION", "TreatmentSession", session.getSessionCode(),
                "Cap nhat thong tin kham cho benh nhan " + session.getPatient().getFullName());
        return session;
    }

    /**
     * Hoan tat phien kham: chuyen check-in sang DONE va lich hen sang COMPLETED,
     * tu dong lap hoa don (neu co dich vu) va de xuat he so ca phuc tap (neu nhap hop le),
     * tat ca trong cung 1 transaction.
     */
    @Transactional
    public void completeExamination(Long sessionId, java.math.BigDecimal complexCaseCoefficient, String complexCaseReason) {
        TreatmentSession session = treatmentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));

        ExaminationForm form = new ExaminationForm();
        form.setSessionId(sessionId);
        form.setSymptom(session.getSymptom());
        form.setDiagnosis(session.getDiagnosis());
        form.setTreatmentPlan(session.getTreatmentPlan());
        form.setDoctorNote(session.getDoctorNote());
        form.setComplexCaseCoefficient(complexCaseCoefficient);
        form.setComplexCaseReason(complexCaseReason);
        completeExamination(sessionId, form);
    }

    @Transactional
    public void completeExamination(Long sessionId, ExaminationForm form) {
        if (form == null) {
            throw new BusinessException("Khong xac dinh duoc thong tin kham.");
        }
        TreatmentSession session = treatmentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phien kham."));
        if (session.getStatus() != TreatmentSessionStatus.OPEN) {
            throw new BusinessException("Phien kham da ket thuc.");
        }

        session.setSymptom(form.getSymptom());
        session.setDiagnosis(form.getDiagnosis());
        session.setTreatmentPlan(form.getTreatmentPlan());
        session.setDoctorNote(form.getDoctorNote());

        java.math.BigDecimal complexCaseCoefficient = form.getComplexCaseCoefficient();
        String complexCaseReason = form.getComplexCaseReason();
        if (session.getDiagnosis() == null || session.getDiagnosis().isBlank()) {
            throw new BusinessException("Vui long nhap chan doan truoc khi hoan tat kham.");
        }
        if (complexCaseCoefficient != null
                && (complexCaseCoefficient.compareTo(MIN_COMPLEX_CASE_COEFFICIENT) < 0
                    || complexCaseCoefficient.compareTo(MAX_COMPLEX_CASE_COEFFICIENT) > 0)) {
            throw new BusinessException("He so ca phuc tap phai trong khoang tu 0.1 den 0.5.");
        }

        // Lich hen tai kham (tai kham): bac si tich co/khong khi hoan tat phien kham.
        if (form.isFollowUp()) {
            LocalDate followUpDate = form.getFollowUpDate();
            if (followUpDate == null) {
                throw new BusinessException("Vui long chon ngay kham lai cho lich hen tai kham.");
            }
            Appointment followUp = appointmentService.createFollowUp(
                    session.getPatient(),
                    session.getDoctor(),
                    session.getAppointment() != null ? session.getAppointment().getService() : null,
                    followUpDate);
            session.setFollowUpDate(followUpDate);
            session.setFollowUpAppointment(followUp);
        }

        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);

        VisitCheckin checkin = session.getCheckin();
        checkin.setStatus(CheckinStatus.DONE);
        visitCheckinRepository.save(checkin);

        Appointment appointment = session.getAppointment();
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        appointmentStatusLogService.log(appointment, oldStatus, AppointmentStatus.COMPLETED, "Hoan tat kham benh");

        invoiceService.createInvoiceIfEligible(sessionId);

        if (complexCaseCoefficient != null) {
            complexCaseCoefficientService.proposeFromExam(sessionId, session.getDoctor(), complexCaseCoefficient, complexCaseReason);
        }

        auditLogService.log("COMPLETE_EXAMINATION", "TreatmentSession", session.getSessionCode(),
                "Hoan tat kham benh nhan " + session.getPatient().getFullName());
    }
}
