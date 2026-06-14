package com.smartdental.service;

import com.smartdental.dto.form.AppointmentForm;
import com.smartdental.dto.form.PatientForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.DentalService;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.Patient;
import com.smartdental.entity.WorkShift;
import com.smartdental.enums.AppointmentSource;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import com.smartdental.enums.WorkShiftDayType;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.DentalServiceRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.EmployeeRepository;
import com.smartdental.repository.HolidayRepository;
import com.smartdental.repository.PatientRepository;
import com.smartdental.repository.WorkShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.Set;

/**
 * Nghiep vu dat lich kham va theo doi trang thai (UC2.4 - Dang ky lich kham benh nhan,
 * UC2.5 - Theo doi lich kham).
 * Phong/ghe khong the chon truc tiep: he thong tu ke thua tu lich truc bac si CONFIRMED
 * (DoctorShiftStatus.APPROVED) theo bac si + ngay kham + ca kham.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^0\\d{9}$");

    private static final Set<AppointmentStatus> FINAL_STATUSES = EnumSet.of(
            AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

    private static final Set<AppointmentStatus> LOCKED_FOR_RESCHEDULE = EnumSet.of(
            AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS, AppointmentStatus.COMPLETED);

    private static final int MAX_APPOINTMENTS_PER_PATIENT_PER_WEEK = 3;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final DentalServiceRepository dentalServiceRepository;
    private final WorkShiftRepository workShiftRepository;
    private final DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;
    private final HolidayRepository holidayRepository;
    private final PatientManagementService patientManagementService;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;
    private final AppointmentStatusLogService appointmentStatusLogService;

    @Transactional(readOnly = true)
    public Page<Appointment> search(String keyword, Long doctorId, Long patientId, AppointmentStatus status,
                                      LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return appointmentRepository.search(kw, doctorId, patientId, status, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.List<Appointment> searchWeek(String keyword, Long doctorId, Long patientId, AppointmentStatus status,
                                                     LocalDate weekStart, LocalDate weekEnd) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return appointmentRepository.findByDateRange(kw, doctorId, patientId, status, weekStart, weekEnd);
    }

    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay lich kham."));
    }

    /**
     * Tim lich truc bac si CONFIRMED (APPROVED) theo bac si + ngay + ca, dung de xem truoc
     * phong/ghe se duoc gan cho lich kham. Tra ve null neu khong co lich truc phu hop.
     */
    @Transactional(readOnly = true)
    public DoctorShiftRegistration findConfirmedShift(Long doctorId, LocalDate date, Long workShiftId) {
        if (doctorId == null || date == null || workShiftId == null) {
            return null;
        }
        return doctorShiftRegistrationRepository.findConfirmedByDoctorDateAndShift(doctorId, date, workShiftId)
                .orElse(null);
    }

    @Transactional
    public Appointment create(AppointmentForm form, AppointmentSource source) {
        validateDateAndShift(form);

        Patient patient = resolvePatient(form);
        Employee doctor = resolveDoctor(form.getDoctorId());
        DentalService service = resolveService(form.getServiceId());
        WorkShift workShift = resolveWorkShift(form.getWorkShiftId());

        if (holidayRepository.existsActiveHolidayOnDate(form.getAppointmentDate())) {
            throw new BusinessException("Ngay duoc chon la ngay nghi cua phong kham.");
        }
        if (workShift.getDayType() != null && !workShift.getDayType().appliesTo(form.getAppointmentDate())) {
            throw new BusinessException("Ca kham nay khong ap dung cho ngay kham da chon.");
        }
        LocalTime startTime = resolveArrivalTime(form, workShift);
        LocalTime endTime = workShift.getEndTime();

        DoctorShiftRegistration registration = null;
        if (doctor != null) {
            registration = doctorShiftRegistrationRepository
                    .findConfirmedByDoctorDateAndShift(doctor.getId(), form.getAppointmentDate(), workShift.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Bac si chua co lich truc duoc duyet cho ngay va ca kham da chon."));

            if (appointmentRepository.existsDoctorConflict(
                    doctor.getId(), form.getAppointmentDate(), startTime, endTime, null)) {
                throw new BusinessException("Bac si da co lich kham trung khung gio nay.");
            }
            if (appointmentRepository.existsChairConflict(
                    registration.getChair().getId(), form.getAppointmentDate(), startTime, endTime, null)) {
                throw new BusinessException("Ghe nha khoa da duoc su dung trong khung gio nay.");
            }

            long activeCount = appointmentRepository.countActiveByDoctorShiftRegistration(registration.getId());
            if (activeCount >= workShift.getMaxAppointments()) {
                throw new BusinessException("Ca kham nay cua bac si da day, vui long chon ca khac.");
            }
        }

        if (appointmentRepository.existsPatientConflictOnShift(
                patient.getId(), form.getAppointmentDate(), workShift.getId(), null)) {
            throw new BusinessException("Benh nhan da co lich kham trong ca nay cua ngay da chon.");
        }

        LocalDate weekStart = form.getAppointmentDate().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        if (form.getAppointmentDate().getDayOfWeek() == DayOfWeek.MONDAY) {
            weekStart = form.getAppointmentDate();
        }
        LocalDate weekEnd = weekStart.plusDays(6);
        long weeklyCount = appointmentRepository.countPatientAppointmentsInWeek(patient.getId(), weekStart, weekEnd, null);
        if (weeklyCount >= MAX_APPOINTMENTS_PER_PATIENT_PER_WEEK) {
            throw new BusinessException("Benh nhan da dat toi da " + MAX_APPOINTMENTS_PER_PATIENT_PER_WEEK + " lich kham trong tuan nay.");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(codeGeneratorService.nextCode(CodePrefix.APPOINTMENT));
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setWorkShift(workShift);
        appointment.setDoctorShiftRegistration(registration);
        appointment.setRoom(registration != null ? registration.getRoom() : null);
        appointment.setChair(registration != null ? registration.getChair() : null);
        appointment.setAppointmentDate(form.getAppointmentDate());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setNote(form.getNote());
        appointment.setSource(source);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentRepository.save(appointment);
        appointmentStatusLogService.log(appointment, null, AppointmentStatus.PENDING, "Tao lich kham");
        auditLogService.log("CREATE_APPOINTMENT", "Appointment", appointment.getAppointmentCode(),
                "Tao lich kham cho benh nhan " + patient.getFullName() + " ngay " + form.getAppointmentDate());
        return appointment;
    }

    /**
     * Tao lich hen tai kham (follow-up) tu phien kham vua hoan tat.
     * Khac voi {@link #create}: khong bat buoc bac si da co lich truc duoc duyet vao ngay tai kham
     * (vi ngay tai kham thuong cach 1 thang, lich truc chua dang ky). Lich tao o trang thai PENDING,
     * gio den mac dinh la dau ca; phong/ghe se duoc gan khi le tan xac nhan.
     */
    @Transactional
    public Appointment createFollowUp(Patient patient, Employee doctor, DentalService service, LocalDate followUpDate) {
        if (patient == null) {
            throw new BusinessException("Khong xac dinh duoc benh nhan de tao lich tai kham.");
        }
        if (followUpDate == null) {
            throw new BusinessException("Vui long chon ngay tai kham.");
        }
        if (followUpDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Ngay tai kham khong the o trong qua khu.");
        }
        if (holidayRepository.existsActiveHolidayOnDate(followUpDate)) {
            throw new BusinessException("Ngay tai kham la ngay nghi cua phong kham, vui long chon ngay khac.");
        }

        WorkShiftDayType dayType = followUpDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || followUpDate.getDayOfWeek() == DayOfWeek.SUNDAY
                ? WorkShiftDayType.WEEKEND : WorkShiftDayType.WEEKDAY;
        List<WorkShift> shifts = workShiftRepository.findActiveForDayType(CommonStatus.ACTIVE, dayType);
        if (shifts.isEmpty()) {
            throw new BusinessException("Khong co ca lam viec phu hop cho ngay tai kham da chon.");
        }
        WorkShift workShift = shifts.get(0);

        // Neu bac si tinh co da co lich truc duyet vao ngay/ca nay thi gan luon phong/ghe.
        DoctorShiftRegistration registration = doctor == null ? null
                : doctorShiftRegistrationRepository
                        .findConfirmedByDoctorDateAndShift(doctor.getId(), followUpDate, workShift.getId())
                        .orElse(null);

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(codeGeneratorService.nextCode(CodePrefix.APPOINTMENT));
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setWorkShift(workShift);
        appointment.setDoctorShiftRegistration(registration);
        appointment.setRoom(registration != null ? registration.getRoom() : null);
        appointment.setChair(registration != null ? registration.getChair() : null);
        appointment.setAppointmentDate(followUpDate);
        appointment.setStartTime(workShift.getStartTime());
        appointment.setEndTime(workShift.getEndTime());
        appointment.setNote("Lich hen tai kham (tu dong tao tu phien kham truoc).");
        appointment.setSource(AppointmentSource.RECEPTIONIST);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentRepository.save(appointment);
        appointmentStatusLogService.log(appointment, null, AppointmentStatus.PENDING, "Tao lich hen tai kham");
        auditLogService.log("CREATE_FOLLOW_UP_APPOINTMENT", "Appointment", appointment.getAppointmentCode(),
                "Tao lich hen tai kham cho benh nhan " + patient.getFullName() + " ngay " + followUpDate);
        return appointment;
    }

    @Transactional
    public Appointment update(AppointmentForm form) {
        Appointment appointment = getById(form.getId());
        if (FINAL_STATUSES.contains(appointment.getStatus())) {
            throw new BusinessException("Khong the chinh sua lich kham da o trang thai cuoi.");
        }
        validateDateAndShift(form);

        Employee doctor = resolveDoctor(form.getDoctorId());
        DentalService service = resolveService(form.getServiceId());
        WorkShift workShift = resolveWorkShift(form.getWorkShiftId());
        if (workShift.getDayType() != null && !workShift.getDayType().appliesTo(form.getAppointmentDate())) {
            throw new BusinessException("Ca kham nay khong ap dung cho ngay kham da chon.");
        }
        LocalTime startTime = resolveArrivalTime(form, workShift);
        LocalTime endTime = workShift.getEndTime();

        boolean rescheduling = !java.util.Objects.equals(doctor != null ? doctor.getId() : null,
                        appointment.getDoctor() != null ? appointment.getDoctor().getId() : null)
                || !form.getAppointmentDate().equals(appointment.getAppointmentDate())
                || !workShift.getId().equals(appointment.getWorkShift() != null ? appointment.getWorkShift().getId() : null);

        if (rescheduling && LOCKED_FOR_RESCHEDULE.contains(appointment.getStatus())) {
            throw new BusinessException("Lich kham da check-in/dang kham/hoan thanh, khong the doi bac si/ngay/ca.");
        }

        if (holidayRepository.existsActiveHolidayOnDate(form.getAppointmentDate())) {
            throw new BusinessException("Ngay duoc chon la ngay nghi cua phong kham.");
        }

        DoctorShiftRegistration registration = appointment.getDoctorShiftRegistration();
        if (rescheduling) {
            registration = null;
            if (doctor != null) {
                registration = doctorShiftRegistrationRepository
                        .findConfirmedByDoctorDateAndShift(doctor.getId(), form.getAppointmentDate(), workShift.getId())
                        .orElseThrow(() -> new BusinessException(
                                "Bac si chua co lich truc duoc duyet cho ngay va ca kham da chon."));

                if (appointmentRepository.existsDoctorConflict(
                        doctor.getId(), form.getAppointmentDate(), startTime, endTime, appointment.getId())) {
                    throw new BusinessException("Bac si da co lich kham trung khung gio nay.");
                }
                if (appointmentRepository.existsChairConflict(
                        registration.getChair().getId(), form.getAppointmentDate(), startTime, endTime, appointment.getId())) {
                    throw new BusinessException("Ghe nha khoa da duoc su dung trong khung gio nay.");
                }
                long activeCount = appointmentRepository.countActiveByDoctorShiftRegistration(registration.getId());
                if (activeCount >= workShift.getMaxAppointments()) {
                    throw new BusinessException("Ca kham nay cua bac si da day, vui long chon ca khac.");
                }
            }
            if (appointmentRepository.existsPatientConflictOnShift(
                    appointment.getPatient().getId(), form.getAppointmentDate(), workShift.getId(), appointment.getId())) {
                throw new BusinessException("Benh nhan da co lich kham trong ca nay cua ngay da chon.");
            }
        } else if (doctor != null) {
            if (appointmentRepository.existsDoctorConflict(
                    doctor.getId(), form.getAppointmentDate(), startTime, endTime, appointment.getId())) {
                throw new BusinessException("Bac si da co lich kham trung khung gio nay.");
            }
        }

        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setWorkShift(workShift);
        appointment.setDoctorShiftRegistration(registration);
        appointment.setRoom(registration != null ? registration.getRoom() : null);
        appointment.setChair(registration != null ? registration.getChair() : null);
        appointment.setAppointmentDate(form.getAppointmentDate());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setNote(form.getNote());

        appointmentRepository.save(appointment);
        auditLogService.log("UPDATE_APPOINTMENT", "Appointment", appointment.getAppointmentCode(),
                "Cap nhat lich kham " + appointment.getAppointmentCode());
        return appointment;
    }

    @Transactional
    public void changeStatus(Long id, AppointmentStatus newStatus, String note, String cancelReason) {
        Appointment appointment = getById(id);
        AppointmentStatus oldStatus = appointment.getStatus();

        if (FINAL_STATUSES.contains(oldStatus)) {
            throw new BusinessException("Lich kham da o trang thai cuoi, khong the thay doi them.");
        }
        if (newStatus == null) {
            throw new BusinessException("Vui long chon trang thai moi.");
        }
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new BusinessException("Khong the chuyen tu trang thai " + oldStatus + " sang " + newStatus + ".");
        }
        if (newStatus == AppointmentStatus.CANCELLED && (cancelReason == null || cancelReason.isBlank())) {
            throw new BusinessException("Vui long nhap ly do huy lich.");
        }

        appointment.setStatus(newStatus);
        if (newStatus == AppointmentStatus.CANCELLED) {
            appointment.setCancelReason(cancelReason);
        }
        appointmentRepository.save(appointment);

        appointmentStatusLogService.log(appointment, oldStatus, newStatus, note);
        auditLogService.log("CHANGE_APPOINTMENT_STATUS", "Appointment", appointment.getAppointmentCode(),
                "Chuyen trang thai lich kham " + appointment.getAppointmentCode() + " tu " + oldStatus + " sang " + newStatus);
    }

    private boolean isValidTransition(AppointmentStatus from, AppointmentStatus to) {
        if (from == to) {
            return false;
        }
        return switch (from) {
            case PENDING -> to == AppointmentStatus.CONFIRMED || to == AppointmentStatus.CANCELLED;
            case CONFIRMED -> to == AppointmentStatus.CHECKED_IN || to == AppointmentStatus.CANCELLED
                    || to == AppointmentStatus.NO_SHOW;
            case CHECKED_IN -> to == AppointmentStatus.IN_PROGRESS || to == AppointmentStatus.CANCELLED;
            case IN_PROGRESS -> to == AppointmentStatus.COMPLETED;
            default -> false;
        };
    }

    private Patient resolvePatient(AppointmentForm form) {
        if (form.getPatientId() != null) {
            return patientRepository.findById(form.getPatientId())
                    .orElseThrow(() -> new BusinessException("Khong tim thay benh nhan."));
        }
        if (form.getPatientFullName() == null || form.getPatientFullName().isBlank()) {
            throw new BusinessException("Vui long chon benh nhan hoac nhap thong tin benh nhan moi.");
        }
        if (form.getPatientPhone() == null || !PHONE_PATTERN.matcher(form.getPatientPhone()).matches()) {
            throw new BusinessException("So dien thoai benh nhan phai gom 10 chu so va bat dau bang so 0.");
        }
        return patientRepository.findByPhone(form.getPatientPhone())
                .orElseGet(() -> {
                    PatientForm patientForm = new PatientForm();
                    patientForm.setFullName(form.getPatientFullName());
                    patientForm.setPhone(form.getPatientPhone());
                    patientForm.setEmail(form.getPatientEmail());
                    patientForm.setAddress(form.getPatientAddress());
                    if (form.getPatientDateOfBirth() != null && !form.getPatientDateOfBirth().isBlank()) {
                        patientForm.setDateOfBirth(LocalDate.parse(form.getPatientDateOfBirth()));
                    }
                    if (form.getPatientGender() != null && !form.getPatientGender().isBlank()) {
                        patientForm.setGender(com.smartdental.enums.Gender.valueOf(form.getPatientGender()));
                    }
                    return patientManagementService.create(patientForm);
                });
    }

    private Employee resolveDoctor(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        Employee doctor = employeeRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException("Khong tim thay bac si."));
        if (doctor.getPosition() != EmployeePosition.DOCTOR) {
            throw new BusinessException("Nhan vien duoc chon khong phai bac si.");
        }
        if (doctor.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BusinessException("Bac si hien khong hoat dong.");
        }
        return doctor;
    }

    private DentalService resolveService(Long serviceId) {
        if (serviceId == null) {
            return null;
        }
        return dentalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException("Khong tim thay dich vu."));
    }

    private WorkShift resolveWorkShift(Long workShiftId) {
        if (workShiftId == null) {
            throw new BusinessException("Vui long chon ca kham.");
        }
        return workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new BusinessException("Khong tim thay ca kham."));
    }

    private void validateDateAndShift(AppointmentForm form) {
        if (form.getAppointmentDate() == null) {
            throw new BusinessException("Vui long chon ngay kham.");
        }
        if (form.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Khong the dat lich cho ngay trong qua khu.");
        }
    }

    /**
     * Benh nhan chi chon CA + GIO DEN. Gio den phai nam trong khung gio cua ca da chon.
     * Tra ve gio den (= gio bat dau cua lich kham); gio ket thuc lay theo gio ket thuc cua ca.
     */
    private LocalTime resolveArrivalTime(AppointmentForm form, WorkShift workShift) {
        LocalTime arrival = form.getArrivalTime();
        if (arrival == null) {
            throw new BusinessException("Vui long chon gio den kham.");
        }
        if (workShift != null && workShift.getStartTime() != null && workShift.getEndTime() != null) {
            if (arrival.isBefore(workShift.getStartTime()) || !arrival.isBefore(workShift.getEndTime())) {
                throw new BusinessException("Gio den phai nam trong khung gio cua ca lam viec ("
                        + workShift.getStartTime() + " - " + workShift.getEndTime() + ").");
            }
        }
        return arrival;
    }
}
