package com.smartdental.service;

import com.smartdental.entity.Appointment;
import com.smartdental.entity.AppointmentStatusLog;
import com.smartdental.enums.AppointmentStatus;
import com.smartdental.repository.AppointmentStatusLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ghi nhat ky thay doi trang thai lich kham (UC2.5).
 */
@Service
@RequiredArgsConstructor
public class AppointmentStatusLogService {

    private final AppointmentStatusLogRepository appointmentStatusLogRepository;

    @Transactional
    public void log(Appointment appointment, AppointmentStatus oldStatus, AppointmentStatus newStatus, String note) {
        AppointmentStatusLog statusLog = new AppointmentStatusLog();
        statusLog.setAppointment(appointment);
        statusLog.setOldStatus(oldStatus);
        statusLog.setNewStatus(newStatus);
        statusLog.setChangedBy(currentUsername());
        statusLog.setNote(note);
        appointmentStatusLogRepository.save(statusLog);
    }

    @Transactional(readOnly = true)
    public List<AppointmentStatusLog> findByAppointmentId(Long appointmentId) {
        return appointmentStatusLogRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
