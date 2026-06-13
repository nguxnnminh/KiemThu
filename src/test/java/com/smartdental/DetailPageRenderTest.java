package com.smartdental;

import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.PayrollStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.PayrollSlipRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.VisitCheckinRepository;
import com.smartdental.service.ExaminationService;
import com.smartdental.service.RegisteredServiceManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem tra render (Thymeleaf parse + nap du lieu) cua cac trang chi tiet sau khi redesign bo cuc.
 * Cung bao ve chong loi LazyInitialization khi open-in-view = false.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DetailPageRenderTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PatientRepository patientRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private VisitCheckinRepository visitCheckinRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private TreatmentSessionRepository treatmentSessionRepository;
    @Autowired private DentalServiceRepository dentalServiceRepository;
    @Autowired private RegisteredServiceManagementService registeredServiceManagementService;
    @Autowired private ExaminationService examinationService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PayrollSlipRepository payrollSlipRepository;

    private Long buildInvoiceId() {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        LocalDate examDate = LocalDate.of(2027, 4, 1);

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("RND900001");
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
        checkin.setQueueNumber(900001);
        checkin.setQueueDate(examDate);
        checkin.setArrivalStatus(ArrivalStatus.ON_TIME);
        checkin.setStatus(CheckinStatus.IN_EXAM);
        checkin = visitCheckinRepository.save(checkin);

        MedicalRecord record = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordCode("BARND900001");
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("LSRND900001");
        session.setAppointment(appointment);
        session.setMedicalRecord(record);
        session.setPatient(patient);
        session.setDoctor(doctor);
        session.setCheckin(checkin);
        session.setExaminationDate(examDate);
        session.setDiagnosis("Sau rang");
        session.setStatus(TreatmentSessionStatus.OPEN);
        session = treatmentSessionRepository.save(session);

        DentalService service = dentalServiceRepository.findByServiceCode("DV001").orElseThrow();
        RegisteredServiceForm form = new RegisteredServiceForm();
        form.setSessionId(session.getId());
        form.setServiceId(service.getId());
        form.setQuantity(1);
        registeredServiceManagementService.register(form);

        examinationService.completeExamination(session.getId(), null, null);
        Invoice invoice = invoiceRepository.findByTreatmentSessionId(session.getId()).orElseThrow();
        return invoice.getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void invoiceDetailRendersWithNewLayout() throws Exception {
        Long invoiceId = buildInvoiceId();
        mockMvc.perform(get("/clinical/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("detail-layout")))
                .andExpect(content().string(containsString("Tổng kết")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void slipDetailRendersWithNewLayout() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        PayrollSlip slip = new PayrollSlip();
        slip.setSlipCode("PLRND900001");
        slip.setDoctor(doctor);
        slip.setPayrollMonth(4);
        slip.setPayrollYear(2027);
        slip.setTotalSalary(new BigDecimal("0.00"));
        slip.setStatus(PayrollStatus.DRAFT);
        slip = payrollSlipRepository.save(slip);

        mockMvc.perform(get("/payroll/slips/{id}", slip.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("summary-strip")));
    }
}
