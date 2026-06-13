package com.smartdental.service;

import com.smartdental.dto.form.CheckinForm;
import com.smartdental.entity.Appointment;
import com.smartdental.entity.Employee;
import com.smartdental.entity.User;
import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.enums.ArrivalStatus;
import com.smartdental.enums.CheckinStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.AppointmentRepository;
import com.smartdental.repository.UserRepository;
import com.smartdental.repository.VisitCheckinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tiep don benh nhan: check-in, hang doi kham (UC3.1).
 */
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final VisitCheckinRepository visitCheckinRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AppointmentStatusLogService appointmentStatusLogService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Appointment> findConfirmedForCheckin(LocalDate date, Long doctorId, Long workShiftId, String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return appointmentRepository.findConfirmedForCheckin(date, doctorId, workShiftId, kw);
    }

    @Transactional(readOnly = true)
    public List<VisitCheckin> findQueue(LocalDate date, CheckinStatus status, Long doctorId) {
        return visitCheckinRepository.findByQueueDateAndStatus(date, status, doctorId);
    }

    @Transactional
    public VisitCheckin checkin(CheckinForm form) {
        if (form.getAppointmentId() == null) {
            throw new BusinessException("Vui long chon lich hen can check-in.");
        }
        Appointment appointment = appointmentRepository.findById(form.getAppointmentId())
                .orElseThrow(() -> new BusinessException("Khong tim thay lich hen."));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Chi co the check-in lich hen o trang thai da xac nhan.");
        }
        if (visitCheckinRepository.findActiveByAppointmentId(appointment.getId()).isPresent()) {
            throw new BusinessException("Lich hen nay da duoc check-in.");
        }
        if (form.getArrivalStatus() == null || form.getArrivalStatus().isBlank()) {
            throw new BusinessException("Vui long chon tinh trang den kham.");
        }

        ArrivalStatus arrivalStatus;
        try {
            arrivalStatus = ArrivalStatus.valueOf(form.getArrivalStatus());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Tinh trang den kham khong hop le.");
        }

        LocalDate today = LocalDate.now();
        int nextQueueNumber = visitCheckinRepository.findMaxQueueNumber(today).orElse(0) + 1;

        VisitCheckin checkin = new VisitCheckin();
        checkin.setAppointment(appointment);
        checkin.setPatient(appointment.getPatient());
        checkin.setReceptionist(currentEmployee());
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setQueueNumber(nextQueueNumber);
        checkin.setQueueDate(today);
        checkin.setArrivalStatus(arrivalStatus);
        checkin.setInitialSymptoms(form.getInitialSymptoms());
        checkin.setNote(form.getNote());
        checkin.setStatus(CheckinStatus.WAITING);
        visitCheckinRepository.save(checkin);

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentRepository.save(appointment);
        appointmentStatusLogService.log(appointment, oldStatus, AppointmentStatus.CHECKED_IN, "Tiep don benh nhan");

        auditLogService.log("CHECKIN_PATIENT", "VisitCheckin", appointment.getAppointmentCode(),
                "Tiep don benh nhan " + appointment.getPatient().getFullName()
                        + ", so thu tu " + nextQueueNumber);
        return checkin;
    }

    @Transactional
    public void cancel(Long checkinId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Vui long nhap ly do huy check-in.");
        }
        VisitCheckin checkin = visitCheckinRepository.findById(checkinId)
                .orElseThrow(() -> new BusinessException("Khong tim thay phieu check-in."));
        if (checkin.getStatus() != CheckinStatus.WAITING) {
            throw new BusinessException("Chi co the huy check-in dang o trang thai cho kham.");
        }

        checkin.setStatus(CheckinStatus.CANCELLED);
        checkin.setCancelReason(reason);
        visitCheckinRepository.save(checkin);

        Appointment appointment = checkin.getAppointment();
        AppointmentStatus oldStatus = appointment.getStatus();
        if (oldStatus == AppointmentStatus.CHECKED_IN) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
            appointmentStatusLogService.log(appointment, oldStatus, AppointmentStatus.CONFIRMED,
                    "Huy check-in: " + reason);
        }

        auditLogService.log("CANCEL_CHECKIN", "VisitCheckin", appointment.getAppointmentCode(),
                "Huy check-in benh nhan " + appointment.getPatient().getFullName() + ", ly do: " + reason);
    }

    @Transactional(readOnly = true)
    public long countWaiting(LocalDate date) {
        return visitCheckinRepository.countByQueueDateAndStatus(date, CheckinStatus.WAITING);
    }

    private Employee currentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        User user = userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                .orElse(null);
        return user != null ? user.getEmployee() : null;
    }
}
