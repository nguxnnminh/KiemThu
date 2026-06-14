package com.smartdental;

import com.smartdental.dto.form.AppointmentForm;
import com.smartdental.dto.form.PaymentForm;
import com.smartdental.dto.form.PayrollSlipCreateForm;
import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.dto.form.ServicePriceForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.Chair;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.RegisteredService;
import com.smartdental.entity.Room;
import com.smartdental.entity.ServiceCategory;
import com.smartdental.entity.ServicePrice;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.enums.Gender;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.PriceStatus;
import com.smartdental.enums.ServiceUnit;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.ChairRepository;
import com.smartdental.repository.ComplexCaseCoefficientRepository;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.RegisteredServiceRepository;
import com.smartdental.repository.RoomRepository;
import com.smartdental.repository.ServiceCategoryRepository;
import com.smartdental.repository.ServicePriceRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.VisitCheckinRepository;
import com.smartdental.repository.WorkShiftRepository;
import com.smartdental.service.AppointmentService;
import com.smartdental.service.InvoiceService;
import com.smartdental.service.PaymentService;
import com.smartdental.service.PayrollSlipService;
import com.smartdental.service.RegisteredServiceManagementService;
import com.smartdental.service.ServicePriceManagementService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase6FullUseCaseCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private DentalServiceRepository dentalServiceRepository;

    @Autowired
    private ServicePriceRepository servicePriceRepository;

    @Autowired
    private ServicePriceManagementService servicePriceManagementService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private VisitCheckinRepository visitCheckinRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private TreatmentSessionRepository treatmentSessionRepository;

    @Autowired
    private RegisteredServiceManagementService registeredServiceManagementService;

    @Autowired
    private RegisteredServiceRepository registeredServiceRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ChairRepository chairRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ComplexCaseCoefficientRepository complexCaseCoefficientRepository;

    @Autowired
    private PayrollSlipService payrollSlipService;

    @Test
    void servicePriceWorkflowExpiresOldPriceAndRejectsBackdatedEffectiveDate() {
        DentalService service = createService("P6SP");

        ServicePriceForm firstForm = new ServicePriceForm();
        firstForm.setServiceId(service.getId());
        firstForm.setPrice(new BigDecimal("100000"));
        firstForm.setEffectiveFrom(LocalDate.of(2031, 1, 1));
        firstForm.setReason("Gia dau tien");
        ServicePrice first = servicePriceManagementService.createNewPrice(firstForm, "test");

        ServicePriceForm secondForm = new ServicePriceForm();
        secondForm.setServiceId(service.getId());
        secondForm.setPrice(new BigDecimal("120000"));
        secondForm.setEffectiveFrom(LocalDate.of(2031, 2, 1));
        secondForm.setReason("Dieu chinh gia");
        ServicePrice second = servicePriceManagementService.createNewPrice(secondForm, "test");

        ServicePrice expiredFirst = servicePriceRepository.findById(first.getId()).orElseThrow();
        assertEquals(PriceStatus.EXPIRED, expiredFirst.getStatus());
        assertEquals(LocalDate.of(2031, 1, 31), expiredFirst.getEffectiveTo());
        assertEquals(PriceStatus.ACTIVE, second.getStatus());

        ServicePriceForm invalidForm = new ServicePriceForm();
        invalidForm.setServiceId(service.getId());
        invalidForm.setPrice(new BigDecimal("130000"));
        invalidForm.setEffectiveFrom(LocalDate.of(2031, 1, 15));
        invalidForm.setReason("Ngay hieu luc khong hop le");

        assertThrows(BusinessException.class,
                () -> servicePriceManagementService.createNewPrice(invalidForm, "test"));
    }

    @Test
    void appointmentUsesApprovedDoctorShiftAndRejectsMissingApprovedShift() {
        Patient patient = createPatient("P6AP");
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        DentalService service = dentalServiceRepository.findByServiceCode("DV001").orElseThrow();
        WorkShift shift = workShiftRepository.findByShiftCode("CA001").orElseThrow();
        LocalDate date = LocalDate.of(2031, 1, 6);
        DoctorShiftRegistration registration = createApprovedShift("P6AS001", doctor, shift, date);

        AppointmentForm form = new AppointmentForm();
        form.setPatientId(patient.getId());
        form.setDoctorId(doctor.getId());
        form.setServiceId(service.getId());
        form.setWorkShiftId(shift.getId());
        form.setAppointmentDate(date);
        form.setArrivalTime(LocalTime.of(9, 0));

        Appointment appointment = appointmentService.create(form, AppointmentSource.RECEPTIONIST);

        assertEquals(registration.getId(), appointment.getDoctorShiftRegistration().getId());
        assertEquals(registration.getRoom().getId(), appointment.getRoom().getId());
        assertEquals(registration.getChair().getId(), appointment.getChair().getId());

        AppointmentForm invalid = new AppointmentForm();
        invalid.setPatientId(patient.getId());
        invalid.setDoctorId(doctor.getId());
        invalid.setServiceId(service.getId());
        invalid.setWorkShiftId(shift.getId());
        invalid.setAppointmentDate(LocalDate.of(2031, 1, 7));
        invalid.setArrivalTime(LocalTime.of(9, 0));

        assertThrows(BusinessException.class,
                () -> appointmentService.create(invalid, AppointmentSource.RECEPTIONIST));
    }

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void confirmedShiftEndpointReturnsRoomAndChairForApprovedShift() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        WorkShift shift = workShiftRepository.findByShiftCode("CA002").orElseThrow();
        LocalDate date = LocalDate.of(2031, 1, 8);
        DoctorShiftRegistration registration = createApprovedShift("P6AS002", doctor, shift, date);

        mockMvc.perform(get("/schedule/appointments/confirmed-shift")
                        .param("doctorId", String.valueOf(doctor.getId()))
                        .param("workShiftId", String.valueOf(shift.getId()))
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(registration.getRoom().getName())))
                .andExpect(content().string(containsString(registration.getChair().getName())));
    }

    @Test
    void registeredServiceCanBeUpdatedBeforeInvoiceAndCannotBeUpdatedAfterInvoice() {
        DentalService service = createService("P6RS");
        createInitialPrice(service, new BigDecimal("100000"), LocalDate.now().minusDays(1));
        TreatmentSession session = createOpenSession("P6RS001", LocalDate.of(2031, 1, 9));

        RegisteredServiceForm registerForm = new RegisteredServiceForm();
        registerForm.setSessionId(session.getId());
        registerForm.setServiceId(service.getId());
        registerForm.setQuantity(1);
        RegisteredService registered = registeredServiceManagementService.register(registerForm);

        RegisteredServiceForm updateForm = new RegisteredServiceForm();
        updateForm.setQuantity(2);
        updateForm.setDiscountAmount(new BigDecimal("10000"));
        updateForm.setToothNumber(16);
        updateForm.setNote("Cap nhat so luong");
        registeredServiceManagementService.update(registered.getId(), updateForm);

        RegisteredService updated = registeredServiceRepository.findById(registered.getId()).orElseThrow();
        assertEquals(2, updated.getQuantity());
        assertEquals(new BigDecimal("190000.00"), updated.getTotalAmount());
        assertEquals(16, updated.getToothNumber());

        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);
        invoiceService.createInvoiceIfEligible(session.getId());

        RegisteredServiceForm lateUpdate = new RegisteredServiceForm();
        lateUpdate.setQuantity(3);
        lateUpdate.setDiscountAmount(BigDecimal.ZERO);
        assertThrows(BusinessException.class,
                () -> registeredServiceManagementService.update(registered.getId(), lateUpdate));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void invoicePartialPaymentAndFilterAreSupported() throws Exception {
        DentalService service = createService("P6IV");
        createInitialPrice(service, new BigDecimal("100000"), LocalDate.now().minusDays(1));
        TreatmentSession session = createOpenSession("P6IV001", LocalDate.of(2031, 1, 10));
        RegisteredServiceForm serviceForm = new RegisteredServiceForm();
        serviceForm.setSessionId(session.getId());
        serviceForm.setServiceId(service.getId());
        serviceForm.setQuantity(1);
        registeredServiceManagementService.register(serviceForm);

        session.setStatus(TreatmentSessionStatus.COMPLETED);
        treatmentSessionRepository.save(session);
        Invoice invoice = invoiceService.createInvoiceIfEligible(session.getId());

        PaymentForm paymentForm = new PaymentForm();
        paymentForm.setInvoiceId(invoice.getId());
        paymentForm.setAmount(new BigDecimal("40000"));
        paymentForm.setPaymentMethod("CASH");
        paymentService.collect(paymentForm);

        Invoice partial = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PARTIAL, partial.getStatus());
        assertEquals(new BigDecimal("40000.00"), partial.getPaidAmount());
        assertEquals(new BigDecimal("60000.00"), partial.getRemainingAmount());

        mockMvc.perform(get("/clinical/invoices").param("status", "PARTIAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thanh toán một phần")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void medicalRecordsListAndSearchAreAvailable() throws Exception {
        createOpenSession("P6MR001", LocalDate.of(2031, 1, 11));

        mockMvc.perform(get("/clinical/medical-records"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hồ sơ bệnh án")));

        mockMvc.perform(get("/clinical/medical-records").param("keyword", "BN0001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BN0001")));
    }

    @Test
    void payrollSlipCreationIsBlockedWhenComplexCoefficientIsPending() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        WorkShift shift = workShiftRepository.findByShiftCode("CA001").orElseThrow();
        LocalDate workDate = LocalDate.of(2031, 2, 3);
        createApprovedShift("P6PR001", doctor, shift, workDate);
        TreatmentSession session = createOpenSession("P6PR001", workDate);

        ComplexCaseCoefficient coefficient = new ComplexCaseCoefficient();
        coefficient.setCoefficientCode("P6CC001");
        coefficient.setTreatmentSession(session);
        coefficient.setDoctor(doctor);
        coefficient.setCoefficient(new BigDecimal("0.30"));
        coefficient.setReason("Ca phuc tap cho test");
        coefficient.setStatus(ApprovalStatus.PENDING);
        coefficient.setProposedBy("doctor");
        complexCaseCoefficientRepository.save(coefficient);

        PayrollSlipCreateForm form = new PayrollSlipCreateForm();
        form.setDoctorId(doctor.getId());
        form.setPayrollYear(2031);
        form.setPayrollMonth(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> payrollSlipService.create(form));
        assertEquals("Con he so ca phuc tap dang cho duyet trong thang nay. Vui long xu ly truoc khi lap phieu luong.",
                ex.getMessage());
    }

    private DentalService createService(String codeSuffix) {
        String unique = codeSuffix + Long.toString(System.nanoTime(), 36);
        ServiceCategory category = new ServiceCategory();
        category.setCategoryCode("C" + unique);
        category.setName("Nhom " + unique);
        category.setDescription("Nhom test Phase 6");
        category.setColorHex("#3366ff");
        category.setStatus(CommonStatus.ACTIVE);
        serviceCategoryRepository.save(category);

        DentalService service = new DentalService();
        service.setServiceCode("D" + unique);
        service.setName("Dich vu " + unique);
        service.setCategory(category);
        service.setUnit(ServiceUnit.LAN);
        service.setDurationMinutes(30);
        service.setStatus(CommonStatus.ACTIVE);
        return dentalServiceRepository.save(service);
    }

    private Patient createPatient(String codeSuffix) {
        String unique = codeSuffix + Long.toString(System.nanoTime(), 36);
        Patient patient = new Patient();
        patient.setPatientCode("B" + unique);
        patient.setFullName("Benh nhan " + unique);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patient.setPhone("09" + Long.toString(System.nanoTime(), 10).substring(2, 10));
        patient.setStatus(CommonStatus.ACTIVE);
        return patientRepository.save(patient);
    }

    private ServicePrice createInitialPrice(DentalService service, BigDecimal price, LocalDate effectiveFrom) {
        ServicePriceForm form = new ServicePriceForm();
        form.setServiceId(service.getId());
        form.setPrice(price);
        form.setEffectiveFrom(effectiveFrom);
        form.setReason("Gia dau tien");
        return servicePriceManagementService.createNewPrice(form, "test");
    }

    private DoctorShiftRegistration createApprovedShift(String code, Employee doctor, WorkShift shift, LocalDate date) {
        Room room = roomRepository.findAll().stream().findFirst().orElseThrow();
        Chair chair = chairRepository.findAll().stream()
                .filter(c -> c.getRoom().getId().equals(room.getId()))
                .findFirst()
                .orElseGet(() -> chairRepository.findAll().stream().findFirst().orElseThrow());

        DoctorShiftRegistration registration = new DoctorShiftRegistration();
        registration.setRegistrationCode(code + Long.toString(System.nanoTime(), 36));
        registration.setDoctor(doctor);
        registration.setWorkShift(shift);
        registration.setWorkDate(date);
        registration.setRoom(room);
        registration.setChair(chair);
        registration.setStatus(DoctorShiftStatus.APPROVED);
        registration.setApprovedBy("admin");
        return doctorShiftRegistrationRepository.save(registration);
    }

    private TreatmentSession createOpenSession(String code, LocalDate examDate) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("P6A" + Long.toString(System.nanoTime(), 36));
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
        checkin.setQueueNumber(900);
        checkin.setQueueDate(examDate);
        checkin.setArrivalStatus(ArrivalStatus.ON_TIME);
        checkin.setStatus(CheckinStatus.IN_EXAM);
        checkin = visitCheckinRepository.save(checkin);

        MedicalRecord record = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordCode("P6MR" + Long.toString(System.nanoTime(), 36));
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("P6S" + Long.toString(System.nanoTime(), 36));
        session.setAppointment(appointment);
        session.setMedicalRecord(record);
        session.setPatient(patient);
        session.setDoctor(doctor);
        session.setCheckin(checkin);
        session.setExaminationDate(examDate);
        session.setSymptom("Dau rang");
        session.setDiagnosis("Sau rang");
        session.setTreatmentPlan("Dieu tri");
        session.setStatus(TreatmentSessionStatus.OPEN);
        TreatmentSession saved = treatmentSessionRepository.save(session);
        assertNotNull(saved.getId());
        return saved;
    }
}
