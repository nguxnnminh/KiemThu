package com.smartdental.service;

import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.DentalServiceRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class InvoiceServiceTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RegisteredServiceManagementService registeredServiceManagementService;

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
    private com.smartdental.repository.EmployeeRepository employeeRepository;

    @Autowired
    private DentalServiceRepository dentalServiceRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private TreatmentSession createOpenSession(String suffix) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        LocalDate examDate = LocalDate.of(2027, 1, 1);

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("INV" + suffix);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(examDate);
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
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
                    r.setRecordCode("BAINV" + suffix);
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("LSINV" + suffix);
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

    private void registerActiveService(TreatmentSession session) {
        DentalService service = dentalServiceRepository.findByServiceCode("DV001").orElseThrow();
        RegisteredServiceForm form = new RegisteredServiceForm();
        form.setSessionId(session.getId());
        form.setServiceId(service.getId());
        form.setQuantity(1);
        registeredServiceManagementService.register(form);
    }

    @Test
    void createsInvoiceWhenActiveServicesExist() {
        TreatmentSession session = createOpenSession("100001");
        registerActiveService(session);
        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);

        Invoice invoice = invoiceService.createInvoiceIfEligible(session.getId());

        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals(new BigDecimal("100000.00"), invoice.getFinalAmount());
        assertEquals(session.getPatient().getId(), invoice.getPatient().getId());
    }

    @Test
    void returnsNullWhenNoActiveServices() {
        TreatmentSession session = createOpenSession("100002");
        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);

        Invoice invoice = invoiceService.createInvoiceIfEligible(session.getId());

        assertNull(invoice);
        assertEquals(java.util.Optional.empty(), invoiceRepository.findByTreatmentSessionId(session.getId()));
    }

    @Test
    void isIdempotentWhenCalledTwice() {
        TreatmentSession session = createOpenSession("100003");
        registerActiveService(session);
        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);

        Invoice first = invoiceService.createInvoiceIfEligible(session.getId());
        Invoice second = invoiceService.createInvoiceIfEligible(session.getId());

        assertEquals(first.getId(), second.getId());
        assertEquals(1, invoiceRepository.findByTreatmentSessionId(session.getId()).map(i -> 1).orElse(0));
    }
}
