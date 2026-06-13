package com.smartdental;

import com.smartdental.dto.form.ComplexCaseCoefficientForm;
import com.smartdental.dto.form.ExaminationForm;
import com.smartdental.dto.form.PaymentForm;
import com.smartdental.dto.form.RegisteredServiceForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Invoice;
import com.smartdental.entity.MedicalRecord;
import com.smartdental.entity.Patient;
import com.smartdental.entity.PayrollItem;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.ApprovalStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.enums.Gender;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.PayrollStatus;
import com.smartdental.enums.TreatmentSessionStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.ComplexCaseCoefficientRepository;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.InvoiceRepository;
import com.smartdental.repository.MedicalRecordRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.PayrollItemRepository;
import com.smartdental.repository.PayrollSlipRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.VisitCheckinRepository;
import com.smartdental.repository.WorkShiftRepository;
import com.smartdental.service.ComplexCaseCoefficientService;
import com.smartdental.service.ExaminationService;
import com.smartdental.service.PayrollSlipService;
import com.smartdental.dto.form.PayrollSlipCreateForm;
import com.smartdental.service.PaymentService;
import com.smartdental.service.RegisteredServiceManagementService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kiem thu tong the + hardening cuoi: hoan thien 2 ton dong Phan 4
 * (de xuat he so ca cheo bac si, pending complex case khoa lap phieu luong)
 * va kiem tra lien ket nghiep vu end-to-end Nhom 2 -> Nhom 3 -> Nhom 4.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Phase5HardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private VisitCheckinRepository visitCheckinRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private TreatmentSessionRepository treatmentSessionRepository;

    @Autowired
    private ComplexCaseCoefficientRepository complexCaseCoefficientRepository;

    @Autowired
    private ComplexCaseCoefficientService complexCaseCoefficientService;

    @Autowired
    private DentalServiceRepository dentalServiceRepository;

    @Autowired
    private RegisteredServiceManagementService registeredServiceManagementService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;

    @Autowired
    private PayrollSlipService payrollSlipService;

    @Autowired
    private PayrollSlipRepository payrollSlipRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    @Autowired
    private com.smartdental.service.PayrollReportService payrollReportService;

    @Autowired
    private com.smartdental.service.PayrollExcelExportService payrollExcelExportService;

    /** Tao mot bac si BS0002 (khac BS0001) chi de dung trong test, khong dung migration moi. */
    private Employee createSecondDoctor() {
        return employeeRepository.findByEmployeeCode("BS0002").orElseGet(() -> {
            Employee doctor = new Employee();
            doctor.setEmployeeCode("BS0002");
            doctor.setFullName("Tran Thi Bac Si Hai");
            doctor.setDateOfBirth(LocalDate.of(1990, 1, 1));
            doctor.setGender(Gender.FEMALE);
            doctor.setPhone("0901000099");
            doctor.setEmail("doctor2@smartdental.vn");
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

    /**
     * Tao mot chuoi day du: Appointment (CONFIRMED) -> VisitCheckin (DONE) -> MedicalRecord -> TreatmentSession (COMPLETED)
     * cho mot benh nhan + bac si chi dinh, voi chan doan da nhap.
     */
    private TreatmentSession createCompletedSession(String suffix, Employee doctor, LocalDate examDate) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode("LK9" + suffix);
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
        checkin.setQueueNumber(1);
        checkin.setQueueDate(examDate);
        checkin.setArrivalStatus(ArrivalStatus.ON_TIME);
        checkin.setStatus(CheckinStatus.DONE);
        checkin = visitCheckinRepository.save(checkin);

        MedicalRecord record = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    MedicalRecord r = new MedicalRecord();
                    r.setRecordCode("BA9" + suffix);
                    r.setPatient(patient);
                    r.setStatus(CommonStatus.ACTIVE);
                    return medicalRecordRepository.save(r);
                });

        TreatmentSession session = new TreatmentSession();
        session.setSessionCode("LS9" + suffix);
        session.setAppointment(appointment);
        session.setMedicalRecord(record);
        session.setPatient(patient);
        session.setDoctor(doctor);
        session.setCheckin(checkin);
        session.setExaminationDate(examDate);
        session.setDiagnosis("Sau rang so 16");
        session.setStatus(TreatmentSessionStatus.COMPLETED);
        return treatmentSessionRepository.save(session);
    }

    private void runAs(String username, String role, Runnable action) {
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            action.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previous);
        }
    }

    // ---------- 5.1: De xuat he so ca phuc tap cheo bac si ----------

    @Test
    @Order(1)
    void doctorCannotProposeComplexCaseForAnotherDoctorsSession() {
        Employee doctorA = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        Employee doctorB = createSecondDoctor();

        TreatmentSession sessionOfDoctorB = createCompletedSession("510001", doctorB, LocalDate.of(2026, 5, 10));

        ComplexCaseCoefficientForm form = new ComplexCaseCoefficientForm();
        form.setTreatmentSessionId(sessionOfDoctorB.getId());
        form.setCoefficient(new BigDecimal("0.2"));
        form.setReason("De xuat tu bac si khac");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> runAs("doctor", "DOCTOR", () -> complexCaseCoefficientService.propose(form, doctorA)));
        assertTrue(ex.getMessage().contains("chinh minh"));
    }

    @Test
    @Order(2)
    void doctorCanProposeComplexCaseForOwnSession() {
        Employee doctorB = createSecondDoctor();
        TreatmentSession sessionOfDoctorB = treatmentSessionRepository.findBySessionCode("LS9510001").orElseThrow();

        ComplexCaseCoefficientForm form = new ComplexCaseCoefficientForm();
        form.setTreatmentSessionId(sessionOfDoctorB.getId());
        form.setCoefficient(new BigDecimal("0.2"));
        form.setReason("Ca kham phuc tap, nhieu rang sau");

        ComplexCaseCoefficient saved = runAsReturning("doctor2", "DOCTOR",
                () -> complexCaseCoefficientService.propose(form, doctorB));

        assertEquals(ApprovalStatus.PENDING, saved.getStatus());
        assertEquals(doctorB.getId(), saved.getDoctor().getId());
    }

    private <T> T runAsReturning(String username, String role, java.util.function.Supplier<T> action) {
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previous);
        }
    }

    // ---------- 5.2: Pending complex case khoa lap phieu luong ----------

    @Test
    @Order(3)
    void pendingComplexCaseBlocksPayrollSlipCreation() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        WorkShift caSang = workShiftRepository.findByShiftCode("CA001").orElseThrow();

        LocalDate workDate = LocalDate.of(2026, 7, 8);
        DoctorShiftRegistration reg = new DoctorShiftRegistration();
        reg.setRegistrationCode("CT900201");
        reg.setDoctor(doctor);
        reg.setWorkShift(caSang);
        reg.setWorkDate(workDate);
        reg.setStatus(DoctorShiftStatus.APPROVED);
        reg.setApprovedBy("admin");
        reg = doctorShiftRegistrationRepository.save(reg);

        // Phien kham hoan tat thuoc ca truc nay
        TreatmentSession session = createCompletedSession("708001", doctor, workDate);
        session.getAppointment().setDoctorShiftRegistration(reg);
        appointmentRepository.save(session.getAppointment());

        // De xuat he so ca phuc tap, dang cho duyet (PENDING)
        ComplexCaseCoefficientForm form = new ComplexCaseCoefficientForm();
        form.setTreatmentSessionId(session.getId());
        form.setCoefficient(new BigDecimal("0.3"));
        form.setReason("Ca kham nhieu rang");
        ComplexCaseCoefficient coefficient = runAsReturning("doctor", "DOCTOR",
                () -> complexCaseCoefficientService.propose(form, doctor));

        // Lap phieu luong thang 7/2026 bi chan vi con PENDING
        PayrollSlipCreateForm slipForm = new PayrollSlipCreateForm();
        slipForm.setDoctorId(doctor.getId());
        slipForm.setPayrollYear(2026);
        slipForm.setPayrollMonth(7);

        BusinessException ex = assertThrows(BusinessException.class, () -> payrollSlipService.create(slipForm));
        assertTrue(ex.getMessage().contains("cho duyet"));

        // Sau khi tu choi -> khong tinh vao patientCoef, lap phieu thanh cong
        complexCaseCoefficientService.reject(coefficient.getId(), rejectForm("Khong dat yeu cau"));

        PayrollSlip slip = payrollSlipService.create(slipForm);
        assertEquals(PayrollStatus.DRAFT, slip.getStatus());

        List<PayrollItem> items = payrollItemRepository.findBySlipId(slip.getId());
        assertEquals(1, items.size());
        PayrollItem item = items.get(0);
        // patientCoefficient = 0 vi he so bi tu choi
        assertEquals(new BigDecimal("0.00"), item.getPatientCoefficientSnapshot());
    }

    private com.smartdental.dto.form.ComplexCaseDecisionForm rejectForm(String reason) {
        com.smartdental.dto.form.ComplexCaseDecisionForm form = new com.smartdental.dto.form.ComplexCaseDecisionForm();
        form.setRejectReason(reason);
        return form;
    }

    @Test
    @Order(4)
    void approvedComplexCaseIsIncludedInPatientCoefficient() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        WorkShift caChieu = workShiftRepository.findByShiftCode("CA002").orElseThrow();

        LocalDate workDate = LocalDate.of(2026, 8, 12);
        DoctorShiftRegistration reg = new DoctorShiftRegistration();
        reg.setRegistrationCode("CT900301");
        reg.setDoctor(doctor);
        reg.setWorkShift(caChieu);
        reg.setWorkDate(workDate);
        reg.setStatus(DoctorShiftStatus.APPROVED);
        reg.setApprovedBy("admin");
        reg = doctorShiftRegistrationRepository.save(reg);

        TreatmentSession session = createCompletedSession("812001", doctor, workDate);
        session.getAppointment().setDoctorShiftRegistration(reg);
        appointmentRepository.save(session.getAppointment());

        ComplexCaseCoefficientForm form = new ComplexCaseCoefficientForm();
        form.setTreatmentSessionId(session.getId());
        form.setCoefficient(new BigDecimal("0.25"));
        form.setReason("Ca kham phuc tap");
        ComplexCaseCoefficient coefficient = runAsReturning("doctor", "DOCTOR",
                () -> complexCaseCoefficientService.propose(form, doctor));

        complexCaseCoefficientService.approve(coefficient.getId());

        PayrollSlipCreateForm slipForm = new PayrollSlipCreateForm();
        slipForm.setDoctorId(doctor.getId());
        slipForm.setPayrollYear(2026);
        slipForm.setPayrollMonth(8);

        PayrollSlip slip = payrollSlipService.create(slipForm);
        List<PayrollItem> items = payrollItemRepository.findBySlipId(slip.getId());
        assertEquals(1, items.size());
        PayrollItem item = items.get(0);

        // patientCoefficient = 0.25 (he so duoc duyet)
        assertEquals(new BigDecimal("0.25"), item.getPatientCoefficientSnapshot());
        // Ca chieu 13:30-17:30 = 4 gio, shiftCoefficient seed cho Ca chieu (HSC0002) = 1.1
        assertEquals(new BigDecimal("4.00"), item.getTotalHours());
        // convertedHours = 4 * (1.1 + 0.25) = 5.40
        assertEquals(new BigDecimal("5.40"), item.getConvertedHours());
        // hourlyRate snapshot = 100000, degreeCoefficient (MASTER) = 1.5
        assertEquals(new BigDecimal("100000.00"), item.getHourlyRateSnapshot());
        assertEquals(new BigDecimal("1.50"), item.getDegreeCoefficientSnapshot());
        // amount = 5.40 * 1.5 * 100000 = 810000.00
        assertEquals(new BigDecimal("810000.00"), item.getAmount());
        assertEquals(new BigDecimal("810000.00"), slip.getTotalSalary());
    }

    // ---------- Luong 1: Tu lich kham den hoa don (Nhom 2 -> Nhom 3) ----------

    @Test
    @Order(5)
    void fullFlowFromAppointmentToInvoicePayment() {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        TreatmentSession session = createCompletedSession("999001", doctor, LocalDate.of(2026, 9, 1));
        // Doi trang thai OPEN de cap nhat chan doan qua examinationService nhu luong thuc te
        session.setStatus(TreatmentSessionStatus.OPEN);
        treatmentSessionRepository.save(session);

        // Bac si cap nhat chan doan
        ExaminationForm examForm = new ExaminationForm();
        examForm.setSessionId(session.getId());
        examForm.setSymptom("Dau rang");
        examForm.setDiagnosis("Sau rang so 16");
        examForm.setTreatmentPlan("Han rang composite");
        examinationService.saveExamination(examForm);

        // Ke dich vu co gia (DV001 = 100,000 VND)
        DentalService service = dentalServiceRepository.findByServiceCode("DV001").orElseThrow();
        RegisteredServiceForm serviceForm = new RegisteredServiceForm();
        serviceForm.setSessionId(session.getId());
        serviceForm.setServiceId(service.getId());
        serviceForm.setQuantity(1);
        registeredServiceManagementService.register(serviceForm);

        // Hoan tat kham (tu dong lap hoa don trong cung transaction)
        examinationService.completeExamination(session.getId(), null, null);
        TreatmentSession completed = treatmentSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(TreatmentSessionStatus.COMPLETED, completed.getStatus());

        Invoice invoice = invoiceRepository.findByTreatmentSessionId(session.getId()).orElseThrow();
        assertEquals(InvoiceStatus.UNPAID, invoice.getStatus());
        assertEquals(new BigDecimal("100000.00"), invoice.getFinalAmount());

        // Thu tien mot phan
        PaymentForm partialPayment = new PaymentForm();
        partialPayment.setInvoiceId(invoice.getId());
        partialPayment.setAmount(new BigDecimal("50000"));
        partialPayment.setPaymentMethod("CASH");
        paymentService.collect(partialPayment);

        Invoice partial = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PARTIAL, partial.getStatus());
        assertEquals(new BigDecimal("50000.00"), partial.getRemainingAmount());

        // Thu het so tien con lai
        PaymentForm finalPayment = new PaymentForm();
        finalPayment.setInvoiceId(invoice.getId());
        finalPayment.setAmount(new BigDecimal("50000"));
        finalPayment.setPaymentMethod("CASH");
        paymentService.collect(finalPayment);

        Invoice paid = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PAID, paid.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), paid.getRemainingAmount());
    }

    // ---------- Bao cao luong: phieu CANCELLED khong tinh vao tong ----------

    @Test
    @Order(6)
    void monthlyPayrollExportExcludesCancelledSlipFromTotal() throws Exception {
        Employee doctorA = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        Employee doctorB = createSecondDoctor();

        PayrollSlip cancelled = new PayrollSlip();
        cancelled.setSlipCode("PL900401");
        cancelled.setDoctor(doctorA);
        cancelled.setPayrollYear(2026);
        cancelled.setPayrollMonth(10);
        cancelled.setTotalSalary(new BigDecimal("500000.00"));
        cancelled.setStatus(PayrollStatus.CANCELLED);
        payrollSlipRepository.save(cancelled);

        PayrollSlip draft = new PayrollSlip();
        draft.setSlipCode("PL900402");
        draft.setDoctor(doctorB);
        draft.setPayrollYear(2026);
        draft.setPayrollMonth(10);
        draft.setTotalSalary(new BigDecimal("300000.00"));
        draft.setStatus(PayrollStatus.DRAFT);
        payrollSlipRepository.save(draft);

        List<PayrollSlip> slips = payrollReportService.getMonthlyReport(2026, 10);
        byte[] excel = payrollExcelExportService.exportMonthlyReport(2026, 10, slips);

        try (org.apache.poi.ss.usermodel.Workbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excel))) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            org.apache.poi.ss.usermodel.Row totalRow = sheet.getRow(sheet.getLastRowNum());
            double total = totalRow.getCell(5).getNumericCellValue();
            // Tong chi gom phieu DRAFT (300000), khong gom phieu CANCELLED (500000)
            assertEquals(300000.0, total, 0.001);
        }
    }
}
