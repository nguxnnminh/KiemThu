package com.smartdental.service;

import com.smartdental.dto.form.EmployeeForm;
import com.smartdental.dto.form.LockUserForm;
import com.smartdental.entity.Employee;
import com.smartdental.entity.User;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.UserStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Period;

/**
 * Nghiep vu quan ly nhan vien (UC1.2).
 */
@Service
@RequiredArgsConstructor
public class EmployeeManagementService {

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^0\\d{9}$");

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<Employee> search(String keyword, EmployeePosition position, EmployeeStatus status,
                                   DoctorDegree degree, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return employeeRepository.search(kw, position, status, degree, pageable);
    }

    /** So lieu tong hop hien thi tren stat-row. */
    public record EmployeeStats(long total, long active, long doctors, long receptionists) {
    }

    @Transactional(readOnly = true)
    public EmployeeStats stats() {
        return new EmployeeStats(
                employeeRepository.count(),
                employeeRepository.countByStatus(EmployeeStatus.ACTIVE),
                employeeRepository.countByPosition(EmployeePosition.DOCTOR),
                employeeRepository.countByPosition(EmployeePosition.RECEPTIONIST));
    }

    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay nhan vien."));
    }

    @Transactional
    public Employee create(EmployeeForm form) {
        validate(form, null);

        Employee employee = new Employee();
        employee.setEmployeeCode(codeGeneratorService.nextCode(codePrefixFor(form.getPosition())));
        applyForm(employee, form);

        employeeRepository.save(employee);
        auditLogService.log("CREATE_EMPLOYEE", "Employee", employee.getEmployeeCode(),
                "Tao nhan vien " + employee.getFullName() + " (" + employee.getPosition() + ")");
        return employee;
    }

    @Transactional
    public Employee update(EmployeeForm form) {
        Employee employee = getById(form.getId());
        validate(form, employee);

        boolean hasAccount = userRepository.existsByEmployeeId(employee.getId());
        if (hasAccount && employee.getPosition() != form.getPosition()) {
            throw new BusinessException("Khong the thay doi chuc vu vi nhan vien da co tai khoan hoac du lieu lien quan.");
        }

        applyForm(employee, form);
        employeeRepository.save(employee);
        auditLogService.log("UPDATE_EMPLOYEE", "Employee", employee.getEmployeeCode(),
                "Cap nhat thong tin nhan vien " + employee.getFullName());
        return employee;
    }

    private void applyForm(Employee employee, EmployeeForm form) {
        employee.setFullName(form.getFullName().trim());
        employee.setDateOfBirth(form.getDateOfBirth());
        employee.setGender(form.getGender());
        employee.setPhone(form.getPhone());
        employee.setEmail(form.getEmail().trim());
        employee.setAddress(form.getAddress());
        employee.setPosition(form.getPosition());
        employee.setSpecialty(form.getSpecialty());
        employee.setQualification(form.getQualification());
        employee.setWorkplace(form.getWorkplace());
        employee.setHireDate(form.getHireDate());
        employee.setDegree(form.getDegree());
        if (form.getStatus() != null) {
            employee.setStatus(form.getStatus());
        }
    }

    private void validate(EmployeeForm form, Employee existing) {
        if (form.getFullName() == null || form.getFullName().isBlank()) {
            throw new BusinessException("Vui long nhap ho ten nhan vien.");
        }
        if (form.getPosition() == null) {
            throw new BusinessException("Vui long chon chuc vu.");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            throw new BusinessException("Vui long nhap email.");
        }
        if (form.getPhone() == null || !PHONE_PATTERN.matcher(form.getPhone()).matches()) {
            throw new BusinessException("So dien thoai phai gom 10 chu so va bat dau bang so 0.");
        }
        if (form.getHireDate() == null) {
            throw new BusinessException("Vui long nhap ngay vao lam.");
        }
        if (form.getDateOfBirth() != null) {
            int ageAtHire = Period.between(form.getDateOfBirth(), form.getHireDate()).getYears();
            if (ageAtHire < 22) {
                throw new BusinessException("Nhan vien phai du 22 tuoi tro len tai thoi diem vao lam.");
            }
        }
        if (form.getPosition() == EmployeePosition.DOCTOR) {
            if (form.getSpecialty() == null || form.getSpecialty().isBlank()) {
                throw new BusinessException("Vui long nhap chuyen khoa cho bac si.");
            }
            if (form.getDegree() == null) {
                throw new BusinessException("Vui long chon hoc vi cho bac si.");
            }
        }

        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (employeeRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
                throw new BusinessException("Email da duoc su dung boi nhan vien khac.");
            }
            if (employeeRepository.existsByPhone(form.getPhone())) {
                throw new BusinessException("So dien thoai da duoc su dung boi nhan vien khac.");
            }
        } else {
            if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(form.getEmail().trim(), currentId)) {
                throw new BusinessException("Email da duoc su dung boi nhan vien khac.");
            }
            if (employeeRepository.existsByPhoneAndIdNot(form.getPhone(), currentId)) {
                throw new BusinessException("So dien thoai da duoc su dung boi nhan vien khac.");
            }
        }
    }

    private CodePrefix codePrefixFor(EmployeePosition position) {
        return switch (position) {
            case DOCTOR -> CodePrefix.DOCTOR;
            case RECEPTIONIST -> CodePrefix.RECEPTIONIST;
            case MANAGER -> CodePrefix.MANAGER;
        };
    }

    @Transactional
    public void suspend(Long id, LockUserForm form) {
        Employee employee = getById(id);
        if (form.getReason() == null || form.getReason().isBlank()) {
            throw new BusinessException("Vui long nhap ly do tam khoa.");
        }
        employee.setStatus(EmployeeStatus.SUSPENDED);
        employeeRepository.save(employee);
        auditLogService.log("SUSPEND_EMPLOYEE", "Employee", employee.getEmployeeCode(),
                "Tam khoa nhan vien " + employee.getFullName() + " - Ly do: " + form.getReason());
    }

    @Transactional
    public void reactivate(Long id) {
        Employee employee = getById(id);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);

        User user = findLinkedUser(employee.getId());
        if (user != null && user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedReason(null);
            userRepository.save(user);
        }

        auditLogService.log("REACTIVATE_EMPLOYEE", "Employee", employee.getEmployeeCode(),
                "Kich hoat lai nhan vien " + employee.getFullName());
    }

    @Transactional
    public void deactivate(Long id, LockUserForm form) {
        Employee employee = getById(id);
        if (!canDeactivateEmployee(id)) {
            throw new BusinessException("Khong the cho nhan vien nay nghi viec do con du lieu lien quan dang xu ly.");
        }
        if (form.getReason() == null || form.getReason().isBlank()) {
            throw new BusinessException("Vui long nhap ly do cho nghi viec.");
        }

        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);

        User user = findLinkedUser(employee.getId());
        if (user != null) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedReason("Nhan vien da nghi viec: " + form.getReason());
            userRepository.save(user);
        }

        auditLogService.log("DEACTIVATE_EMPLOYEE", "Employee", employee.getEmployeeCode(),
                "Cho nhan vien " + employee.getFullName() + " nghi viec - Ly do: " + form.getReason());
    }

    /**
     * Hook kiem tra co the cho nhan vien nghi viec hay khong.
     * Cac phan sau (lich kham, bang luong...) se bo sung dieu kien tai day.
     */
    @Transactional(readOnly = true)
    public boolean canDeactivateEmployee(Long employeeId) {
        return true;
    }

    private User findLinkedUser(Long employeeId) {
        return userRepository.findByEmployeeId(employeeId).orElse(null);
    }
}
