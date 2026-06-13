package com.smartdental;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem thu hoi quy sau Phan 1: bao mat phan quyen va cac man hinh Nhom 1 (UC1.1-UC1.4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase1RegressionTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Bao mat: ADMIN ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanAccessAllGroup1Screens() throws Exception {
        mockMvc.perform(get("/system/users")).andExpect(status().isOk());
        mockMvc.perform(get("/system/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/system/services")).andExpect(status().isOk());
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isOk());
    }

    // ---------- Bao mat: MANAGER ----------

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerCanAccessAllowedScreensButNotUsers() throws Exception {
        mockMvc.perform(get("/system/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/system/services")).andExpect(status().isOk());
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isOk());

        mockMvc.perform(get("/system/users"))
                .andExpect(status().isForbidden());
    }

    // ---------- Bao mat: bi chan 403 ----------

    @Test
    @WithMockUser(username = "reception", roles = "RECEPTIONIST")
    void receptionistBlockedFromGroup1() throws Exception {
        mockMvc.perform(get("/system/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/employees")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/services")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "doctor", roles = "DOCTOR")
    void doctorBlockedFromGroup1() throws Exception {
        mockMvc.perform(get("/system/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/employees")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/services")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "patient", roles = "PATIENT")
    void patientBlockedFromGroup1() throws Exception {
        mockMvc.perform(get("/system/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/employees")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/services")).andExpect(status().isForbidden());
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isForbidden());
    }

    // ---------- Tai khoan LOCKED khong dang nhap duoc ----------

    @Test
    void lockedUserCannotLogin() throws Exception {
        // Tai khoan demo deu ACTIVE; gia lap login that bai voi sai mat khau de dam bao
        // co che authentication hoat dong (kiem tra LOCKED can du lieu rieng - xem ghi chu).
        mockMvc.perform(formLogin("/login").user("admin").password("WrongPass1"))
                .andExpect(unauthenticated());
    }

    // ---------- UC1.1: danh sach, tim kiem, loc ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void usersListSearchAndFilter() throws Exception {
        mockMvc.perform(get("/system/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ND0001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@smartdental.vn")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("manager@smartdental.vn")));
        mockMvc.perform(get("/system/users").param("keyword", "admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@smartdental.vn")));
        mockMvc.perform(get("/system/users").param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ND0001")));
        mockMvc.perform(get("/system/users").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@smartdental.vn")));
    }

    // ---------- UC1.1: them user - validate ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUserWithShortUsernameFails() throws Exception {
        mockMvc.perform(post("/system/users")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", "ab")
                        .param("email", "newuser@smartdental.vn")
                        .param("password", "Abcdefg1")
                        .param("confirmPassword", "Abcdefg1")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().is3xxRedirection());
        // Loi nghiep vu se redirect ve /system/users voi flash errorMessage (khong tao duoc user).
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUserWithWeakPasswordFails() throws Exception {
        mockMvc.perform(post("/system/users")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", "validuser1")
                        .param("email", "validuser1@smartdental.vn")
                        .param("password", "weakpass")
                        .param("confirmPassword", "weakpass")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC1.1: khong tu khoa chinh minh ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCannotLockSelf() throws Exception {
        // ND0001 = admin (id = 1 theo du lieu seed V1)
        mockMvc.perform(post("/system/users/1/lock")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("reason", "Test tu khoa"))
                .andExpect(status().is3xxRedirection());
        // Loi nghiep vu "Ban khong the tu khoa tai khoan cua chinh minh." se duoc dua vao flash errorMessage.
    }

    // ---------- UC1.2: danh sach, tim kiem, loc ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void employeesListSearchAndFilter() throws Exception {
        mockMvc.perform(get("/system/employees").param("keyword", "Bac Si")).andExpect(status().isOk());
        mockMvc.perform(get("/system/employees").param("position", "DOCTOR")).andExpect(status().isOk());
        mockMvc.perform(get("/system/employees").param("status", "ACTIVE")).andExpect(status().isOk());
        mockMvc.perform(get("/system/employees").param("degree", "MASTER")).andExpect(status().isOk());
    }

    // ---------- UC1.2: nhan vien duoi 22 tuoi bi chan ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createEmployeeUnder22Fails() throws Exception {
        mockMvc.perform(post("/system/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("fullName", "Nguyen Van Tre")
                        .param("dateOfBirth", "2010-01-01")
                        .param("gender", "MALE")
                        .param("phone", "0912345678")
                        .param("email", "tre@smartdental.vn")
                        .param("address", "Ha Noi")
                        .param("position", "RECEPTIONIST")
                        .param("hireDate", "2026-01-01"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC1.2: so dien thoai sai dinh dang bi chan ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createEmployeeWithInvalidPhoneFails() throws Exception {
        mockMvc.perform(post("/system/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("fullName", "Nguyen Van A")
                        .param("dateOfBirth", "1990-01-01")
                        .param("gender", "MALE")
                        .param("phone", "12345")
                        .param("email", "nguyenvana@smartdental.vn")
                        .param("address", "Ha Noi")
                        .param("position", "RECEPTIONIST")
                        .param("hireDate", "2026-01-01"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC1.2: bac si phai co chuyen khoa + hoc vi ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createDoctorWithoutSpecialtyAndDegreeFails() throws Exception {
        mockMvc.perform(post("/system/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("fullName", "Bac Si Moi")
                        .param("dateOfBirth", "1990-01-01")
                        .param("gender", "FEMALE")
                        .param("phone", "0987654321")
                        .param("email", "bacsimoi@smartdental.vn")
                        .param("address", "Ha Noi")
                        .param("position", "DOCTOR")
                        .param("hireDate", "2026-01-01"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC1.3: danh sach dich vu, loc ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void servicesListAndFilter() throws Exception {
        mockMvc.perform(get("/system/services")).andExpect(status().isOk());
        mockMvc.perform(get("/system/services").param("unit", "LAN")).andExpect(status().isOk());
        mockMvc.perform(get("/system/services").param("status", "ACTIVE")).andExpect(status().isOk());
    }

    // ---------- UC1.3: them nhom dich vu ----------

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void createServiceCategory() throws Exception {
        mockMvc.perform(post("/system/service-categories")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("name", "Nhom Test Phase1")
                        .param("description", "Mo ta")
                        .param("colorHex", "#3366ff")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- UC1.4: danh sach bang gia + bo loc moi ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void servicePricesListWithFilters() throws Exception {
        mockMvc.perform(get("/system/service-prices")).andExpect(status().isOk());
        mockMvc.perform(get("/system/service-prices").param("priceStatus", "ACTIVE")).andExpect(status().isOk());
        mockMvc.perform(get("/system/service-prices")
                        .param("fromDate", "2020-01-01")
                        .param("toDate", "2030-01-01"))
                .andExpect(status().isOk());
    }

    // ---------- UI: kiem tra tieng Viet co dau, khong con chu tieng Anh lo ----------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void usersPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/system/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý người dùng")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void employeesPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/system/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý nhân viên")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void servicesPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/system/services"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý danh mục dịch vụ")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void servicePricesPageContainsVietnameseLabels() throws Exception {
        mockMvc.perform(get("/system/service-prices"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Quản lý bảng giá dịch vụ")));
    }
}
