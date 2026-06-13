package com.smartdental.service;

import com.smartdental.dto.form.LockUserForm;
import com.smartdental.dto.form.ResetPasswordForm;
import com.smartdental.dto.form.UserForm;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Patient;
import com.smartdental.entity.User;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.Role;
import com.smartdental.enums.UserStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Nghiep vu quan ly tai khoan va phan quyen (UC1.1).
 */
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final String USERNAME_PATTERN = "^[a-z0-9_]{6,}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    private static final String PASSWORD_CHARS_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String PASSWORD_CHARS_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PASSWORD_CHARS_DIGIT = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<User> search(String keyword, Role role, UserStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepository.search(kw, role, status, pageable);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay tai khoan."));
    }

    public static String generateRandomPassword() {
        StringBuilder sb = new StringBuilder();
        sb.append(PASSWORD_CHARS_UPPER.charAt(RANDOM.nextInt(PASSWORD_CHARS_UPPER.length())));
        sb.append(PASSWORD_CHARS_LOWER.charAt(RANDOM.nextInt(PASSWORD_CHARS_LOWER.length())));
        sb.append(PASSWORD_CHARS_DIGIT.charAt(RANDOM.nextInt(PASSWORD_CHARS_DIGIT.length())));
        String all = PASSWORD_CHARS_LOWER + PASSWORD_CHARS_UPPER + PASSWORD_CHARS_DIGIT;
        for (int i = 0; i < 7; i++) {
            sb.append(all.charAt(RANDOM.nextInt(all.length())));
        }
        return sb.toString();
    }

    @Transactional
    public User create(UserForm form) {
        validateCommon(form, null);

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new BusinessException("Vui long nhap mat khau.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mat khau nhap lai khong khop.");
        }
        if (!form.getPassword().matches(PASSWORD_PATTERN)) {
            throw new BusinessException("Mat khau phai co toi thieu 8 ky tu, bao gom chu hoa, chu thuong va chu so.");
        }

        User user = new User();
        user.setUserCode(codeGeneratorService.nextCode(CodePrefix.USER));
        user.setUsername(form.getUsername().trim().toLowerCase());
        user.setEmail(form.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        user.setStatus(form.getStatus() == null ? UserStatus.ACTIVE : form.getStatus());
        applyLinks(user, form);

        userRepository.save(user);
        auditLogService.log("CREATE_USER", "User", user.getUserCode(),
                "Tao tai khoan " + user.getUsername() + " voi vai tro " + user.getRole());
        return user;
    }

    @Transactional
    public User update(UserForm form) {
        User user = getById(form.getId());
        validateCommon(form, user.getId());

        Role oldRole = user.getRole();
        user.setEmail(form.getEmail().trim());
        user.setRole(form.getRole());
        user.setStatus(form.getStatus());
        applyLinks(user, form);

        userRepository.save(user);
        auditLogService.log("UPDATE_USER", "User", user.getUserCode(),
                "Cap nhat tai khoan " + user.getUsername());
        if (oldRole != user.getRole()) {
            auditLogService.log("CHANGE_USER_ROLE", "User", user.getUserCode(),
                    "Doi vai tro tai khoan " + user.getUsername() + " tu " + oldRole + " sang " + user.getRole());
        }
        return user;
    }

    private void applyLinks(User user, UserForm form) {
        if (form.getEmployeeId() != null && form.getPatientId() != null) {
            throw new BusinessException("Tai khoan chi duoc lien ket voi mot nhan vien hoac mot benh nhan, khong the ca hai.");
        }

        if (form.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(form.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay nhan vien duoc chon."));

            boolean linkedToOther = (user.getId() == null)
                    ? userRepository.existsByEmployeeId(employee.getId())
                    : userRepository.existsByEmployeeIdAndIdNot(employee.getId(), user.getId());
            if (linkedToOther) {
                throw new BusinessException("Nhan vien nay da duoc lien ket voi mot tai khoan khac.");
            }

            Role expectedRole = mapPositionToRole(employee.getPosition());
            if (expectedRole != null && form.getRole() != expectedRole) {
                throw new BusinessException("Vai tro phai khop voi chuc vu cua nhan vien duoc chon (" + expectedRole + ").");
            }

            user.setEmployee(employee);
            user.setPatient(null);
        } else if (form.getPatientId() != null) {
            Patient patient = patientRepository.findById(form.getPatientId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay benh nhan duoc chon."));

            if (form.getRole() != Role.PATIENT) {
                throw new BusinessException("Vai tro phai la BENH NHAN khi lien ket voi ho so benh nhan.");
            }

            user.setPatient(patient);
            user.setEmployee(null);
        } else {
            user.setEmployee(null);
            user.setPatient(null);
        }
    }

    private Role mapPositionToRole(EmployeePosition position) {
        return switch (position) {
            case DOCTOR -> Role.DOCTOR;
            case RECEPTIONIST -> Role.RECEPTIONIST;
            case MANAGER -> Role.MANAGER;
        };
    }

    private void validateCommon(UserForm form, Long currentId) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            throw new BusinessException("Vui long nhap ten dang nhap.");
        }
        String username = form.getUsername().trim().toLowerCase();
        if (!username.matches(USERNAME_PATTERN)) {
            throw new BusinessException("Ten dang nhap toi thieu 6 ky tu, chi gom chu thuong, so va dau gach duoi.");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new BusinessException("Vui long nhap email.");
        }
        if (form.getRole() == null) {
            throw new BusinessException("Vui long chon vai tro.");
        }

        if (currentId == null) {
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                throw new BusinessException("Ten dang nhap da ton tai trong he thong.");
            }
            if (userRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
                throw new BusinessException("Email da duoc su dung boi tai khoan khac.");
            }
        } else {
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail().trim(), currentId)) {
                throw new BusinessException("Email da duoc su dung boi tai khoan khac.");
            }
        }
    }

    @Transactional
    public void lock(Long id, LockUserForm form, String currentUsername) {
        User user = getById(id);

        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new BusinessException("Ban khong the tu khoa tai khoan cua chinh minh.");
        }
        if (form.getReason() == null || form.getReason().isBlank()) {
            throw new BusinessException("Vui long nhap ly do khoa tai khoan.");
        }
        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE
                && userRepository.countActiveAdmins() <= 1) {
            throw new BusinessException("Khong the khoa tai khoan ADMIN dang hoat dong cuoi cung.");
        }

        user.setStatus(UserStatus.LOCKED);
        user.setLockedReason(form.getReason());
        userRepository.save(user);
        auditLogService.log("LOCK_USER", "User", user.getUserCode(),
                "Khoa tai khoan " + user.getUsername() + " - Ly do: " + form.getReason());
    }

    @Transactional
    public void unlock(Long id, LockUserForm form, String currentUsername) {
        User user = getById(id);

        user.setStatus(UserStatus.ACTIVE);
        user.setLockedReason(null);
        userRepository.save(user);
        auditLogService.log("UNLOCK_USER", "User", user.getUserCode(),
                "Mo khoa tai khoan " + user.getUsername()
                        + (form != null && form.getReason() != null && !form.getReason().isBlank()
                            ? " - Ly do: " + form.getReason() : ""));
    }

    @Transactional
    public String resetPassword(Long id, ResetPasswordForm form) {
        User user = getById(id);

        if (form.getNewPassword() == null || form.getNewPassword().isBlank()) {
            throw new BusinessException("Vui long nhap mat khau moi.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mat khau nhap lai khong khop.");
        }
        if (!form.getNewPassword().matches(PASSWORD_PATTERN)) {
            throw new BusinessException("Mat khau phai co toi thieu 8 ky tu, bao gom chu hoa, chu thuong va chu so.");
        }

        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
        auditLogService.log("RESET_PASSWORD", "User", user.getUserCode(),
                "Dat lai mat khau cho tai khoan " + user.getUsername());
        return form.getNewPassword();
    }
}
