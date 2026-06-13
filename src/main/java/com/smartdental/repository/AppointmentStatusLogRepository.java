package com.smartdental.repository;

import com.smartdental.entity.AppointmentStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentStatusLogRepository extends JpaRepository<AppointmentStatusLog, Long> {

    List<AppointmentStatusLog> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
}
