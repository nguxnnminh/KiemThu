package com.smartdental.service;

import com.smartdental.dto.form.DoctorShiftRegistrationForm;
import com.smartdental.entity.Chair;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Room;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.DoctorShiftStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.RoomStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.ChairRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.HolidayRepository;
import com.smartdental.repository.RoomRepository;
import com.smartdental.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Nghiep vu dang ky / duyet lich truc bac si (UC2.3 - Dang ky lich truc bac si).
 */
@Service
@RequiredArgsConstructor
public class DoctorShiftService {

    private final DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final RoomRepository roomRepository;
    private final ChairRepository chairRepository;
    private final HolidayRepository holidayRepository;
    private final AppointmentRepository appointmentRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<DoctorShiftRegistration> search(Long doctorId, LocalDate fromDate, LocalDate toDate,
                                                  DoctorShiftStatus status, Pageable pageable) {
        return doctorShiftRegistrationRepository.search(doctorId, fromDate, toDate, status, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.List<DoctorShiftRegistration> searchWeek(Long doctorId, LocalDate weekStart, LocalDate weekEnd,
                                                                 DoctorShiftStatus status) {
        return doctorShiftRegistrationRepository.findByDateRange(doctorId, weekStart, weekEnd, status);
    }

    @Transactional(readOnly = true)
    public DoctorShiftRegistration getById(Long id) {
        return doctorShiftRegistrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay dang ky lich truc."));
    }

    @Transactional
    public DoctorShiftRegistration register(DoctorShiftRegistrationForm form) {
        if (form.getDoctorId() == null) {
            throw new BusinessException("Vui long chon bac si.");
        }
        Employee doctor = employeeRepository.findById(form.getDoctorId())
                .orElseThrow(() -> new BusinessException("Khong tim thay bac si."));
        if (doctor.getPosition() != EmployeePosition.DOCTOR) {
            throw new BusinessException("Nhan vien duoc chon khong phai bac si.");
        }
        if (doctor.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException("Bac si hien khong hoat dong.");
        }
        if (form.getWorkShiftId() == null) {
            throw new BusinessException("Vui long chon ca lam viec.");
        }
        WorkShift workShift = workShiftRepository.findById(form.getWorkShiftId())
                .orElseThrow(() -> new BusinessException("Khong tim thay ca lam viec."));
        if (form.getWorkDate() == null) {
            throw new BusinessException("Vui long chon ngay truc.");
        }
        if (form.getWorkDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Khong the dang ky lich truc cho ngay trong qua khu.");
        }
        if (holidayRepository.existsActiveHolidayOnDate(form.getWorkDate())) {
            throw new BusinessException("Ngay duoc chon la ngay nghi cua phong kham.");
        }
        if (workShift.getDayType() != null && !workShift.getDayType().appliesTo(form.getWorkDate())) {
            throw new BusinessException("Ca lam viec nay khong ap dung cho ngay truc da chon.");
        }
        if (doctorShiftRegistrationRepository.existsByDoctorIdAndWorkShiftIdAndWorkDate(
                doctor.getId(), workShift.getId(), form.getWorkDate())) {
            throw new BusinessException("Bac si da dang ky ca lam viec nay trong ngay da chon.");
        }
        if (doctorShiftRegistrationRepository.existsDoctorConflict(
                doctor.getId(), form.getWorkDate(), workShift.getId(), null)) {
            throw new BusinessException("Bac si da co lich truc khac trung ngay va ca lam viec nay.");
        }

        Room room = null;
        if (form.getRoomId() != null) {
            room = roomRepository.findById(form.getRoomId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay phong kham."));
        }

        Chair chair = null;
        if (form.getChairId() != null) {
            chair = chairRepository.findById(form.getChairId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay ghe nha khoa."));
            if (room != null && !chair.getRoom().getId().equals(room.getId())) {
                throw new BusinessException("Ghe nha khoa khong thuoc phong kham da chon.");
            }
            if (room == null) {
                room = chair.getRoom();
            }
            if (doctorShiftRegistrationRepository.existsChairConflict(
                    chair.getId(), form.getWorkDate(), workShift.getId(), null)) {
                throw new BusinessException("Ghe nha khoa da duoc dang ky cho ca lam viec nay trong ngay da chon.");
            }
        }

        DoctorShiftRegistration registration = new DoctorShiftRegistration();
        registration.setRegistrationCode(codeGeneratorService.nextCode(CodePrefix.DOCTOR_SHIFT));
        registration.setDoctor(doctor);
        registration.setWorkShift(workShift);
        registration.setWorkDate(form.getWorkDate());
        registration.setRoom(room);
        registration.setChair(chair);
        registration.setNote(form.getNote());
        registration.setStatus(DoctorShiftStatus.REGISTERED);

        doctorShiftRegistrationRepository.save(registration);
        auditLogService.log("REGISTER_DOCTOR_SHIFT", "DoctorShiftRegistration", registration.getRegistrationCode(),
                "Bac si " + doctor.getFullName() + " dang ky truc " + workShift.getName() + " ngay " + form.getWorkDate());
        return registration;
    }

    @Transactional
    public void cancel(Long id) {
        DoctorShiftRegistration registration = getById(id);
        if (registration.getStatus() == DoctorShiftStatus.CANCELLED) {
            throw new BusinessException("Lich truc da bi huy truoc do.");
        }
        if (appointmentRepository.existsUnfinishedByDoctorShiftRegistration(id)) {
            throw new BusinessException("Khong the huy lich truc vi con lich kham chua hoan thanh gan voi lich truc nay.");
        }
        registration.setStatus(DoctorShiftStatus.CANCELLED);
        doctorShiftRegistrationRepository.save(registration);
        auditLogService.log("CANCEL_DOCTOR_SHIFT", "DoctorShiftRegistration", registration.getRegistrationCode(),
                "Huy dang ky lich truc cua bac si " + registration.getDoctor().getFullName());
    }

    @Transactional
    public void approve(Long id) {
        DoctorShiftRegistration registration = getById(id);
        if (registration.getStatus() != DoctorShiftStatus.REGISTERED) {
            throw new BusinessException("Chi co the duyet dang ky o trang thai cho duyet.");
        }
        Employee doctor = registration.getDoctor();
        if (doctor.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException("Bac si hien khong hoat dong, khong the duyet lich truc.");
        }
        if (registration.getWorkShift().getStatus() != CommonStatus.ACTIVE) {
            throw new BusinessException("Ca lam viec hien khong hoat dong, khong the duyet lich truc.");
        }
        if (registration.getRoom() != null && registration.getRoom().getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("Phong kham hien khong hoat dong, khong the duyet lich truc.");
        }
        if (registration.getChair() != null && registration.getChair().getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException("Ghe nha khoa hien khong hoat dong, khong the duyet lich truc.");
        }
        registration.setStatus(DoctorShiftStatus.APPROVED);
        registration.setApprovedBy(currentUsername());
        doctorShiftRegistrationRepository.save(registration);
        auditLogService.log("APPROVE_DOCTOR_SHIFT", "DoctorShiftRegistration", registration.getRegistrationCode(),
                "Duyet lich truc cua bac si " + registration.getDoctor().getFullName());
    }

    @Transactional
    public void reject(Long id, String reason) {
        DoctorShiftRegistration registration = getById(id);
        if (registration.getStatus() != DoctorShiftStatus.REGISTERED) {
            throw new BusinessException("Chi co the tu choi dang ky o trang thai cho duyet.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui long nhap ly do tu choi.");
        }
        registration.setStatus(DoctorShiftStatus.REJECTED);
        registration.setApprovedBy(currentUsername());
        registration.setNote((registration.getNote() == null ? "" : registration.getNote() + " | ")
                + "Tu choi: " + reason);
        doctorShiftRegistrationRepository.save(registration);
        auditLogService.log("REJECT_DOCTOR_SHIFT", "DoctorShiftRegistration", registration.getRegistrationCode(),
                "Tu choi lich truc cua bac si " + registration.getDoctor().getFullName() + " - Ly do: " + reason);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
