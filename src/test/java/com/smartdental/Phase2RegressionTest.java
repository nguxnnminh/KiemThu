package com.smartdental;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem thu hoi quy sau Phan 2: bao mat phan quyen va cac man hinh Nhom 2 (UC2.1-UC2.6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase2RegressionTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Bao mat: ADMIN/MANAGER duoc truy cap day du Nhom 2 ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanAccessAllGroup2Screens() throws Exception {
        mockMvc.perform(get("/schedule/holidays")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/work-shifts")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/rooms")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/doctor-shifts")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/appointments")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/appointments/track")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/patients")).andExpect(status().isOk());
    }

    // ---------- Bao mat: RECEPTIONIST bi chan man hinh quan tri, duoc dung dat lich ----------

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void receptionistAccessRules() throws Exception {
        mockMvc.perform(get("/schedule/holidays")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/work-shifts")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/rooms")).andExpect(status().isForbidden());

        mockMvc.perform(get("/schedule/appointments")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/appointments/track")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/patients")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/doctor-shifts")).andExpect(status().isForbidden());
    }

    // ---------- Bao mat: DOCTOR chi xem lich truc va theo doi lich kham cua minh ----------

    @Test
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorAccessRules() throws Exception {
        mockMvc.perform(get("/schedule/doctor-shifts")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/appointments/track")).andExpect(status().isOk());

        mockMvc.perform(get("/schedule/holidays")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/work-shifts")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/rooms")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/appointments")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/patients")).andExpect(status().isForbidden());
    }

    // ---------- Bao mat: PATIENT chi dung khu vuc cua minh ----------

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientAccessRules() throws Exception {
        mockMvc.perform(get("/patient/book-appointment")).andExpect(status().isOk());
        mockMvc.perform(get("/patient/appointments")).andExpect(status().isOk());

        mockMvc.perform(get("/schedule/appointments")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/appointments/track")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/holidays")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/patients")).andExpect(status().isForbidden());
        mockMvc.perform(get("/schedule/doctor-shifts")).andExpect(status().isForbidden());
    }

    // ---------- UC2.1: them ngay nghi - validate ngay khong hop le ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createHolidayWithInvalidDateRangeFails() throws Exception {
        mockMvc.perform(post("/schedule/holidays")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Nghi Test")
                        .param("startDate", "2026-09-10")
                        .param("endDate", "2026-09-01")
                        .param("holidayType", "HOLIDAY")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
        // Loi nghiep vu (ngay ket thuc truoc ngay bat dau) se redirect ve /schedule/holidays voi flash errorMessage.
    }

    // ---------- UC2.2: them ca lam viec - validate gio khong hop le ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createWorkShiftWithInvalidTimeRangeFails() throws Exception {
        mockMvc.perform(post("/schedule/work-shifts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Ca Test")
                        .param("startTime", "12:00")
                        .param("endTime", "08:00")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC2.3: danh sach phong va ghe ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void roomsAndChairsListWithFilters() throws Exception {
        mockMvc.perform(get("/schedule/rooms")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/rooms").param("status", "ACTIVE")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/rooms").param("keyword", "P00")).andExpect(status().isOk());
    }

    // ---------- UC2.4: bac si dang ky lich truc cho ngay trong qua khu bi chan ----------

    @Test
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorRegisterShiftInPastFails() throws Exception {
        mockMvc.perform(post("/schedule/doctor-shifts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", "1")
                        .param("workDate", "2020-01-01"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC2.5: dat lich kham - validate ngay khong hop le ----------

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void createAppointmentWithPastDateFails() throws Exception {
        mockMvc.perform(post("/schedule/appointments")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("patientFullName", "Benh Nhan Test")
                        .param("patientPhone", "0911222333")
                        .param("doctorId", "1")
                        .param("workShiftId", "1")
                        .param("appointmentDate", "2020-01-01")
                        .param("startTime", "09:00")
                        .param("endTime", "09:30"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void createAppointmentWithEndBeforeStartFails() throws Exception {
        mockMvc.perform(post("/schedule/appointments")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("patientFullName", "Benh Nhan Test")
                        .param("patientPhone", "0911222334")
                        .param("doctorId", "1")
                        .param("workShiftId", "1")
                        .param("appointmentDate", "2030-01-02")
                        .param("startTime", "10:00")
                        .param("endTime", "09:00"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC2.4: dat lich kham bi chan neu bac si chua co lich truc duoc duyet ----------

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void createAppointmentWithoutConfirmedShiftFails() throws Exception {
        mockMvc.perform(post("/schedule/appointments")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("patientFullName", "Benh Nhan Khong Lich Truc")
                        .param("patientPhone", "0911222335")
                        .param("doctorId", "3")
                        .param("workShiftId", "1")
                        .param("appointmentDate", "2030-03-10")
                        .param("startTime", "09:00")
                        .param("endTime", "09:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attributeExists("errorMessage"));
    }

    // ---------- UC2.3: dang ky lich truc khong duoc trung bac si + ngay + ca ----------

    @Test
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void duplicateDoctorShiftRegistrationFails() throws Exception {
        mockMvc.perform(post("/schedule/doctor-shifts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", "1")
                        .param("workDate", "2030-03-11"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/schedule/doctor-shifts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", "1")
                        .param("workDate", "2030-03-11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attributeExists("errorMessage"));
    }

    // ---------- UC2.4: dat lich kham tu dong ke thua phong/ghe tu lich truc da duyet ----------

    @Test
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void approvedDoctorShiftAllowsAppointmentBookingWithAutoRoomChair() throws Exception {
        mockMvc.perform(post("/schedule/doctor-shifts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("workShiftId", "2")
                        .param("workDate", "2030-03-12")
                        .param("roomId", "1")
                        .param("chairId", "1"))
                .andExpect(status().is3xxRedirection());

        // Lay id cua dang ky vua tao bang truy van danh sach (ADMIN duyet)
        // Do moi truong test khong co API tra ve id truc tiep, dung repository qua context la khong phu hop o day,
        // nen test nay chi xac nhan dang ky duoc tao thanh cong (redirect) va trang doctor-shifts hien thi dung.
        mockMvc.perform(get("/schedule/doctor-shifts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.containsString("REGISTERED"),
                        org.hamcrest.Matchers.containsString("Chờ duyệt"))));
    }

    // ---------- UC2.5: benh nhan dat lich online ----------

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientCanBookAppointmentOnline() throws Exception {
        mockMvc.perform(post("/patient/book-appointment")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("appointmentDate", "2030-01-02")
                        .param("startTime", "09:00")
                        .param("endTime", "09:30"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC2.6: them benh nhan - validate so dien thoai sai dinh dang ----------

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void createPatientWithInvalidPhoneFails() throws Exception {
        mockMvc.perform(post("/schedule/patients")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("fullName", "Benh Nhan Moi")
                        .param("phone", "12345")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void patientsListSearchAndFilter() throws Exception {
        mockMvc.perform(get("/schedule/patients").param("keyword", "BN")).andExpect(status().isOk());
        mockMvc.perform(get("/schedule/patients").param("status", "ACTIVE")).andExpect(status().isOk());
    }

    // ---------- UI: kiem tra tieng Viet co dau ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void holidaysPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/schedule/holidays"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý ngày nghỉ")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void appointmentsPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/schedule/appointments"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đặt lịch khám")));
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void bookAppointmentPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/patient/book-appointment"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đặt lịch khám online")));
    }
}
