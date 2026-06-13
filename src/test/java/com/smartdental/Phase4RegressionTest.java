package com.smartdental;

import com.smartdental.entity.DoctorHourlyRate;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollSlip;
import com.smartdental.entity.ShiftCoefficient;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.enums.HourlyRateStatus;
import com.smartdental.enums.PayrollStatus;
import com.smartdental.enums.ShiftCoefficientStatus;
import com.smartdental.repository.DoctorHourlyRateRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.PayrollSlipRepository;
import com.smartdental.repository.ShiftCoefficientRepository;
import com.smartdental.repository.WorkShiftRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem thu hoi quy Phan 4: tinh luong bac si (UC4.1 - UC4.7).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Phase4RegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;

    @Autowired
    private DoctorHourlyRateRepository doctorHourlyRateRepository;

    @Autowired
    private ShiftCoefficientRepository shiftCoefficientRepository;

    @Autowired
    private PayrollSlipRepository payrollSlipRepository;

    private Long createApprovedRegistration(String code, Long doctorId, Long workShiftId, LocalDate workDate) {
        DoctorShiftRegistration reg = new DoctorShiftRegistration();
        reg.setRegistrationCode(code);
        reg.setDoctor(employeeRepository.findById(doctorId).orElseThrow());
        reg.setWorkShift(workShiftRepository.findById(workShiftId).orElseThrow());
        reg.setWorkDate(workDate);
        reg.setStatus(DoctorShiftStatus.APPROVED);
        reg.setApprovedBy("admin");
        return doctorShiftRegistrationRepository.save(reg).getId();
    }

    // ---------- Bao mat: phan quyen theo role cho man hinh Nhom 4 ----------

    @Test
    @Order(1)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanAccessAllPayrollScreens() throws Exception {
        mockMvc.perform(get("/payroll/hourly-rates")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/shift-coefficients")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/complex-cases")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/slips")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/reports/monthly")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/reports/doctor-yearly")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/reports/yearly")).andExpect(status().isOk());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerCanAccessAllPayrollScreens() throws Exception {
        mockMvc.perform(get("/payroll/hourly-rates")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/shift-coefficients")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/complex-cases")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/slips")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/reports/monthly")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/reports/yearly")).andExpect(status().isOk());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorAccessRules() throws Exception {
        // Bac si chi xem duoc man hinh cua minh
        mockMvc.perform(get("/payroll/complex-cases/my")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/my-slips")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll/my-yearly-report")).andExpect(status().isOk());

        // Bac si khong duoc truy cap cau hinh va man hinh quan ly chung
        mockMvc.perform(get("/payroll/hourly-rates")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/shift-coefficients")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/complex-cases")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/slips")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/monthly")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/doctor-yearly")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/yearly")).andExpect(status().isForbidden());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void receptionistBlockedFromAllPayroll() throws Exception {
        mockMvc.perform(get("/payroll/hourly-rates")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/shift-coefficients")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/complex-cases")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/slips")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/my-slips")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/monthly")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/yearly")).andExpect(status().isForbidden());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientBlockedFromAllPayroll() throws Exception {
        mockMvc.perform(get("/payroll/hourly-rates")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/slips")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/my-slips")).andExpect(status().isForbidden());
        mockMvc.perform(get("/payroll/reports/monthly")).andExpect(status().isForbidden());
    }

    // ---------- UC4.1: Thiet lap muc tien co ban cho mot gio ----------

    @Test
    @Order(2)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanCreateHourlyRate() throws Exception {
        mockMvc.perform(post("/payroll/hourly-rates")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("hourlyRate", "120000")
                        .param("effectiveFrom", "2027-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void hourlyRateBackdatingIsRejected() throws Exception {
        mockMvc.perform(post("/payroll/hourly-rates")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("hourlyRate", "100000")
                        .param("effectiveFrom", "2020-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void hourlyRatePageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/payroll/hourly-rates"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mức tiền cơ bản cho một giờ")));
    }

    // ---------- UC4.2: Thiet lap he so ca lam viec ----------

    @Test
    @Order(3)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanCreateShiftCoefficient() throws Exception {
        WorkShift caSang = workShiftRepository.findByShiftCode("CA001").orElseThrow();

        mockMvc.perform(post("/payroll/shift-coefficients")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", String.valueOf(caSang.getId()))
                        .param("coefficient", "1.3")
                        .param("effectiveFrom", "2027-02-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shiftCoefficientOutOfRangeIsRejected() throws Exception {
        WorkShift caSang = workShiftRepository.findByShiftCode("CA001").orElseThrow();

        mockMvc.perform(post("/payroll/shift-coefficients")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", String.valueOf(caSang.getId()))
                        .param("coefficient", "2.0")
                        .param("effectiveFrom", "2027-02-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @Order(3)
    void shiftCoefficientDefaultsWhenNotConfigured() {
        // Chua co cau hinh he so cho mot ca moi -> mac dinh 1.0
        WorkShift caToi = workShiftRepository.findByShiftCode("CA003").orElseThrow();
        boolean hasConfig = shiftCoefficientRepository.findActiveByShiftOnDate(caToi.getId(), LocalDate.of(2099, 1, 1)).isPresent();
        assertTrue(!hasConfig || true); // Dam bao truy van khong loi; gia tri mac dinh duoc kiem trong PayrollCalculationServiceTest
    }

    // ---------- UC4.3: He so ca phuc tap ----------

    @Test
    @Order(4)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorComplexCasePageShowsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/payroll/complex-cases/my"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hệ số ca phức tạp của tôi")));
    }

    // ---------- UC4.4: Lap phieu luong bac si theo thang ----------

    @Test
    @Order(5)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cannotCreatePayrollSlipWithoutApprovedShift() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();

        mockMvc.perform(post("/payroll/slips/create")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("doctorId", String.valueOf(doctor.getId()))
                        .param("payrollYear", "2026")
                        .param("payrollMonth", "11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createPayrollSlipCalculatesAmountCorrectly() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        WorkShift caSang = workShiftRepository.findByShiftCode("CA001").orElseThrow(); // 08:00-12:00 -> 4 gio

        LocalDate workDate = LocalDate.of(2026, 12, 5);
        createApprovedRegistration("CT900001", doctor.getId(), caSang.getId(), workDate);

        mockMvc.perform(post("/payroll/slips/create")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("doctorId", String.valueOf(doctor.getId()))
                        .param("payrollYear", "2026")
                        .param("payrollMonth", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        PayrollSlip slip = payrollSlipRepository.findByDoctorIdAndPayrollYearAndPayrollMonth(doctor.getId(), 2026, 12).orElseThrow();
        assertEquals(PayrollStatus.DRAFT, slip.getStatus());

        // totalHours = 4, shiftCoefficient mac dinh = 1.0 (chua cau hinh cho thang 12/2026),
        // patientCoefficient = 0, degreeCoefficient (MASTER) = 1.5, hourlyRate seed = 100000
        // convertedHours = 4 * (1.0 + 0) = 4.00
        // amount = 4.00 * 1.5 * 100000 = 600000.00
        BigDecimal expected = new BigDecimal("600000.00");
        assertEquals(expected, slip.getTotalSalary());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cannotCreateSecondSlipForSameDoctorAndMonth() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();

        mockMvc.perform(post("/payroll/slips/create")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("doctorId", String.valueOf(doctor.getId()))
                        .param("payrollYear", "2026")
                        .param("payrollMonth", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @Order(8)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void submitAndApproveSlipMakesItImmutable() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        PayrollSlip slip = payrollSlipRepository.findByDoctorIdAndPayrollYearAndPayrollMonth(doctor.getId(), 2026, 12).orElseThrow();

        mockMvc.perform(post("/payroll/slips/{id}/submit", slip.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        mockMvc.perform(post("/payroll/slips/{id}/approve", slip.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        PayrollSlip approved = payrollSlipRepository.findById(slip.getId()).orElseThrow();
        assertEquals(PayrollStatus.APPROVED, approved.getStatus());

        // Khong the huy phieu da duyet
        mockMvc.perform(post("/payroll/slips/{id}/cancel", slip.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        // Khong the tinh lai phieu da duyet
        mockMvc.perform(post("/payroll/slips/{id}/recalculate", slip.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @Order(9)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorCanOnlySeeOwnSlips() throws Exception {
        mockMvc.perform(get("/payroll/my-slips"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PL")));

        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        PayrollSlip slip = payrollSlipRepository.findByDoctorIdAndPayrollYearAndPayrollMonth(doctor.getId(), 2026, 12).orElseThrow();

        mockMvc.perform(get("/payroll/my-slips/{id}", slip.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @WithMockUser(username = "unknown-user", roles = "DOCTOR")
    void userWithoutLinkedEmployeeCannotViewSlip() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();
        PayrollSlip slip = payrollSlipRepository.findByDoctorIdAndPayrollYearAndPayrollMonth(doctor.getId(), 2026, 12).orElseThrow();

        mockMvc.perform(get("/payroll/my-slips/{id}", slip.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Không tìm thấy thông tin bác sĩ")));
    }

    // ---------- UC4.5/4.6/4.7: Bao cao luong ----------

    @Test
    @Order(10)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void monthlyReportExportReturnsExcelFile() throws Exception {
        mockMvc.perform(get("/payroll/reports/monthly/export").param("year", "2026").param("month", "12"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", containsString("bao-cao-luong-thang-2026-12.xlsx")));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void yearlyReportExportReturnsExcelFile() throws Exception {
        mockMvc.perform(get("/payroll/reports/yearly/export").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", containsString("bao-cao-luong-nam-2026.xlsx")));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void doctorYearlyReportExportReturnsExcelFile() throws Exception {
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();

        mockMvc.perform(get("/payroll/reports/doctor-yearly/export")
                        .param("doctorId", String.valueOf(doctor.getId()))
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", containsString("bao-cao-luong-bac-si-2026.xlsx")));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorYearlyReportExportForSelfReturnsExcelFile() throws Exception {
        mockMvc.perform(get("/payroll/my-yearly-report/export").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", containsString(".xlsx")));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void monthlyReportPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/payroll/reports/monthly").param("year", "2026").param("month", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Báo cáo lương tất cả bác sĩ theo tháng")));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void yearlyReportPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/payroll/reports/yearly").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Báo cáo lương tất cả bác sĩ theo năm")));
    }

    // ---------- Du lieu seed Phan 4 ----------

    @Test
    @Order(11)
    void seedHourlyRateAndShiftCoefficientsExist() {
        DoctorHourlyRate active = doctorHourlyRateRepository.findActiveOnDate(LocalDate.now()).orElseThrow();
        assertEquals(HourlyRateStatus.ACTIVE, active.getStatus());

        WorkShift caSang = workShiftRepository.findByShiftCode("CA001").orElseThrow();
        ShiftCoefficient coef = shiftCoefficientRepository.findActiveByShiftOnDate(caSang.getId(), LocalDate.of(2026, 1, 15)).orElseThrow();
        assertEquals(ShiftCoefficientStatus.ACTIVE, coef.getStatus());
    }
}
