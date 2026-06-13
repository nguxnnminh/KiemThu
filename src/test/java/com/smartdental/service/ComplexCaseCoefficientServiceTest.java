package com.smartdental.service;

import com.smartdental.entity.Appointment;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.Employee;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.Gender;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.EmployeeRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ComplexCaseCoefficientServiceTest {

    @Autowired
    private ComplexCaseCoefficientService complexCaseCoefficientService;

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

    private Employee createSecondDoctor() {
        return employeeRepository.findByEmployeeCode("BS0099").orElseGet(() -> {
            Employee doctor = new Employee();
            doctor.setEmployeeCode("BS0099");
            doctor.setFullName("Bac Si Phu");
            doctor.setDateOfBirth(LocalDate.of(1990, 1, 1));
            doctor.setGender(Gender.FEMALE);
            doctor.setPhone("0901000098");
            doctor.setEmail("doctor99@smartdental.vn");
            doctor.setAddress("Ha Noi");
            doctor.setPosition(EmployeePosition.DOCTOR);
            doctor.setSpecialty("Nha khoa tham my");
            doctor.setQualification("Bac si nha khoa");
            doctor.setWorkplace("Smart Dental");
            doctor.setHireDate(LocalDate.of(2020, 1, 1));
            doctor.setDegree(DoctorDegree.BACHELOR);
            doctor.setStatus(EmployeeStatus.ACTIVE);
            return employeeRepository.save(doctor);
        });
    }

    private TreatmentSession createSession(String suffix, Employee doctor, TreatmentSessionStatus status) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        LocalDate examDate = LocalDate.of(2027, 3, 1);

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("CCC" + suffix);
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
        checkin.setStatus(CheckinStatus.DONE);
        checkin = visitCheckinRepository.save(checkin);

        MedicalRecord record = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordCode("BACCC" + suffix);
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("LSCCC" + suffix);
        session.setAppointment(appointment);
        session.setMedicalRecord(record);
        session.setPatient(patient);
        session.setDoctor(doctor);
        session.setCheckin(checkin);
        session.setExaminationDate(examDate);
        session.setDiagnosis("Sau rang so 16");
        session.setStatus(status);
        return treatmentSessionRepository.save(session);
    }

    @Test
    void findByTreatmentSessionIdReturnsEmptyWhenNoCoefficient() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createSession("300001", doctor, TreatmentSessionStatus.COMPLETED);

        Optional<ComplexCaseCoefficient> result = complexCaseCoefficientService.findByTreatmentSessionId(session.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByTreatmentSessionIdReturnsCoefficientAfterPropose() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createSession("300002", doctor, TreatmentSessionStatus.COMPLETED);

        complexCaseCoefficientService.proposeFromExam(session.getId(), doctor, new BigDecimal("0.2"), "Ca kham phuc tap");

        Optional<ComplexCaseCoefficient> result = complexCaseCoefficientService.findByTreatmentSessionId(session.getId());

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("0.20"), result.get().getCoefficient());
        assertEquals(ApprovalStatus.PENDING, result.get().getStatus());
    }

    @Test
    void proposeFromExamRejectsOutOfRangeCoefficient() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createSession("300003", doctor, TreatmentSessionStatus.COMPLETED);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> complexCaseCoefficientService.proposeFromExam(session.getId(), doctor, new BigDecimal("0.6"), "Khong hop le"));
        assertTrue(ex.getMessage().contains("0.1 den 0.5"));
    }

    @Test
    void proposeFromExamRejectsNonCompletedSession() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createSession("300004", doctor, TreatmentSessionStatus.OPEN);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> complexCaseCoefficientService.proposeFromExam(session.getId(), doctor, new BigDecimal("0.2"), "Chua hoan tat"));
        assertTrue(ex.getMessage().contains("hoan tat"));
    }

    @Test
    void proposeFromExamRejectsDoctorNotOwningSession() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        Employee otherDoctor = createSecondDoctor();
        TreatmentSession session = createSession("300005", doctor, TreatmentSessionStatus.COMPLETED);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> complexCaseCoefficientService.proposeFromExam(session.getId(), otherDoctor, new BigDecimal("0.2"), "Khong phai cua minh"));
        assertTrue(ex.getMessage().contains("chinh minh"));
    }

    @Test
    void proposeFromExamRejectsDuplicateProposal() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createSession("300006", doctor, TreatmentSessionStatus.COMPLETED);

        complexCaseCoefficientService.proposeFromExam(session.getId(), doctor, new BigDecimal("0.2"), "De xuat lan 1");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> complexCaseCoefficientService.proposeFromExam(session.getId(), doctor, new BigDecimal("0.3"), "De xuat lan 2"));
        assertTrue(ex.getMessage().contains("da co de xuat"));
    }
}
