package com.smartdental.service;

import com.smartdental.dto.form.ExaminationForm;
import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.Employee;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.ComplexCaseCoefficientRepository;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.VisitCheckinRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ExaminationServiceTest {

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private VisitCheckinRepository visitCheckinRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private TreatmentSessionRepository treatmentSessionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DentalServiceRepository dentalServiceRepository;

    @Autowired
    private RegisteredServiceManagementService registeredServiceManagementService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ComplexCaseCoefficientRepository complexCaseCoefficientRepository;

    private TreatmentSession createOpenSession(String suffix) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        LocalDate examDate = LocalDate.of(2027, 2, 1);

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("EXM" + suffix);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(examDate);
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setSource(AppointmentSource.RECEPTIONIST);
        appointment = appointmentRepository.save(appointment);

        VisitCheckin checkin = new VisitCheckin();
        checkin.setAppointment(appointment);
        checkin.setPatient(patient);
        checkin.setCheckinTime(LocalDateTime.of(examDate, LocalTime.of(8, 30)));
        checkin.setQueueNumber(Integer.parseInt(suffix));
        checkin.setQueueDate(examDate);
        checkin.setArrivalStatus(ArrivalStatus.ON_TIME);
        checkin.setStatus(CheckinStatus.IN_EXAM);
        checkin = visitCheckinRepository.save(checkin);

        MedicalRecord record = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordCode("BAEXM" + suffix);
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("LSEXM" + suffix);
        session.setAppointment(appointment);
        session.setMedicalRecord(record);
        session.setPatient(patient);
        session.setDoctor(doctor);
        session.setCheckin(checkin);
        session.setExaminationDate(examDate);
        session.setDiagnosis("Sau rang so 16");
        session.setStatus(TreatmentSessionStatus.OPEN);
        return treatmentSessionRepository.save(session);
    }

    @Test
    void completingWithValidCoefficientCreatesPendingComplexCaseCoefficient() {
        TreatmentSession session = createOpenSession("200001");

        examinationService.completeExamination(session.getId(), new BigDecimal("0.3"), "Ca kham phuc tap nhieu rang");

        TreatmentSession completed = treatmentSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(TreatmentSessionStatus.COMPLETED, completed.getStatus());

        ComplexCaseCoefficient coefficient = complexCaseCoefficientRepository.findByTreatmentSessionId(session.getId())
                .orElseThrow();
        assertEquals(ApprovalStatus.PENDING, coefficient.getStatus());
        assertEquals(new BigDecimal("0.30"), coefficient.getCoefficient());
    }

    @Test
    void completingWithoutCoefficientCreatesNoComplexCaseCoefficient() {
        TreatmentSession session = createOpenSession("200002");

        examinationService.completeExamination(session.getId(), null, null);

        assertFalse(complexCaseCoefficientRepository.findByTreatmentSessionId(session.getId()).isPresent());
    }

    @Test
    void savingDraftDoesNotPersistComplexCaseCoefficient() {
        TreatmentSession session = createOpenSession("200003");

        ExaminationForm form = new ExaminationForm();
        form.setSessionId(session.getId());
        form.setSymptom("Dau rang");
        form.setDiagnosis("Sau rang so 16");
        form.setTreatmentPlan("Han rang composite");
        examinationService.saveExamination(form);

        assertFalse(complexCaseCoefficientRepository.findByTreatmentSessionId(session.getId()).isPresent());

        TreatmentSession saved = treatmentSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(TreatmentSessionStatus.OPEN, saved.getStatus());
    }

    @Test
    void completingWithOutOfRangeCoefficientThrowsAndRollsBack() {
        TreatmentSession session = createOpenSession("200004");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> examinationService.completeExamination(session.getId(), new BigDecimal("0.9"), "Khong hop le"));
        assertTrue(ex.getMessage().contains("0.1 den 0.5"));

        TreatmentSession unchanged = treatmentSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(TreatmentSessionStatus.OPEN, unchanged.getStatus());
        assertFalse(complexCaseCoefficientRepository.findByTreatmentSessionId(session.getId()).isPresent());
        assertFalse(invoiceRepository.findByTreatmentSessionId(session.getId()).isPresent());
    }

    @Test
    void completingWithActiveServicesAutoCreatesInvoice() {
        TreatmentSession session = createOpenSession("200005");
        DentalService service = dentalServiceRepository.findByServiceCode("DV001").orElseThrow();
        RegisteredServiceForm serviceForm = new RegisteredServiceForm();
        serviceForm.setSessionId(session.getId());
        serviceForm.setServiceId(service.getId());
        serviceForm.setQuantity(1);
        registeredServiceManagementService.register(serviceForm);

        examinationService.completeExamination(session.getId(), null, null);

        assertTrue(invoiceRepository.findByTreatmentSessionId(session.getId()).isPresent());
    }
}
