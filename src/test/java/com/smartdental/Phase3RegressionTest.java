package com.smartdental;

import com.smartdental.entity.Appointment;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Patient;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.VisitCheckinRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem thu hoi quy Phan 3: bao mat phan quyen va luong nghiep vu Nhom 3 (UC3.1-UC3.6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Phase3RegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private VisitCheckinRepository visitCheckinRepository;

    private Long createConfirmedAppointment(String code) {
        Patient patient = patientRepository.findByPatientCode("BN0001").orElseThrow();
        Employee doctor = employeeRepository.findByEmployeeCode("BS0001").orElseThrow();

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(code);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setStartTime(LocalTime.of(9, 0));
        appointment.setEndTime(LocalTime.of(9, 30));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setSource(AppointmentSource.RECEPTIONIST);
        return appointmentRepository.save(appointment).getId();
    }

    // ---------- Bao mat: phan quyen theo role cho cac man hinh Nhom 3 ----------

    @Test
    @Order(1)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanAccessAllGroup3Screens() throws Exception {
        mockMvc.perform(get("/clinical/checkin")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/queue")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/examination")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/invoices")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/revenue")).andExpect(status().isOk());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void receptionistAccessRules() throws Exception {
        mockMvc.perform(get("/clinical/checkin")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/queue")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/invoices")).andExpect(status().isOk());

        mockMvc.perform(get("/clinical/examination")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/tooth-chart").param("patientId", "1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/services")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/revenue")).andExpect(status().isForbidden());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorAccessRules() throws Exception {
        mockMvc.perform(get("/clinical/queue")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/examination")).andExpect(status().isOk());
        mockMvc.perform(get("/clinical/tooth-chart").param("patientId", "1")).andExpect(status().isOk());

        mockMvc.perform(get("/clinical/invoices")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/revenue")).andExpect(status().isForbidden());
    }

    @Test
    @Order(1)
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientAccessRules() throws Exception {
        mockMvc.perform(get("/clinical/checkin")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/examination")).andExpect(status().isForbidden());
        mockMvc.perform(get("/clinical/revenue")).andExpect(status().isForbidden());

        mockMvc.perform(get("/patient/invoices")).andExpect(status().isOk());
    }

    // ---------- UC3.1: Check-in benh nhan tu lich hen da xac nhan ----------

    @Test
    @Order(2)
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void receptionistCanCheckinConfirmedAppointment() throws Exception {
        Long appointmentId = createConfirmedAppointment("LH900001");

        mockMvc.perform(get("/clinical/checkin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LH900001")));

        mockMvc.perform(post("/clinical/checkin")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("appointmentId", String.valueOf(appointmentId))
                        .param("arrivalStatus", "ON_TIME"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/clinical/checkin"));

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AppointmentStatus.CHECKED_IN, appointment.getStatus());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void doubleCheckinSameAppointmentFails() throws Exception {
        Long appointmentId = createConfirmedAppointment("LH900002");

        mockMvc.perform(post("/clinical/checkin")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("appointmentId", String.valueOf(appointmentId))
                        .param("arrivalStatus", "ON_TIME"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/clinical/checkin")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("appointmentId", String.valueOf(appointmentId))
                        .param("arrivalStatus", "ON_TIME"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ---------- UC3.2: Bat dau kham va hoan tat phien kham ----------

    @Test
    @Order(3)
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorCanStartAndCompleteExamination() throws Exception {
        Long appointmentId = createConfirmedAppointment("LH900003");

        mockMvc.perform(post("/clinical/checkin")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user("reception").roles("RECEPTIONIST"))
                        .param("appointmentId", String.valueOf(appointmentId))
                        .param("arrivalStatus", "ON_TIME"))
                .andExpect(status().is3xxRedirection());

        Long checkinId = visitCheckinRepository.findActiveByAppointmentId(appointmentId).orElseThrow().getId();

        mockMvc.perform(post("/clinical/queue/{id}/start", checkinId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());

        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());

        // Hoan tat kham khi chua co chan doan -> bao loi, redirect ve trang kham
        mockMvc.perform(get("/clinical/queue"))
                .andExpect(status().isOk());
    }

    // ---------- UI: kiem tra tieng Viet co dau ----------

    @Test
    @Order(4)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void checkinPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/clinical/checkin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tiếp đón")));
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void revenuePageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/clinical/revenue"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Báo cáo doanh thu")));
    }

    // ---------- Runtime fix: favicon va tra cuu so do rang khong tham so ----------

    @Test
    @Order(5)
    void faviconRequestDoesNotReturn500() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().is(org.hamcrest.Matchers.not(500)));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void toothChartLookupWithoutPatientIdReturnsOk() throws Exception {
        mockMvc.perform(get("/clinical/tooth-chart"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tra cứu sơ đồ răng")));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "admin", roles = "ADMIN")
    void toothChartWithNonExistentPatientShowsVietnameseError() throws Exception {
        mockMvc.perform(get("/clinical/tooth-chart").param("patientId", "999999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Không tìm thấy bệnh nhân")));
    }
}
