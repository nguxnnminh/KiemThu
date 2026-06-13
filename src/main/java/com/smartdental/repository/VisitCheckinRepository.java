package com.smartdental.repository;

import com.smartdental.entity.VisitCheckin;
import com.smartdental.enums.CheckinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitCheckinRepository extends JpaRepository<VisitCheckin, Long> {

    @Query("select c from VisitCheckin c where c.appointment.id = :appointmentId " +
            "and c.status <> com.smartdental.enums.CheckinStatus.CANCELLED")
    Optional<VisitCheckin> findActiveByAppointmentId(@Param("appointmentId") Long appointmentId);

    long countByQueueDate(LocalDate queueDate);

    @Query("select max(c.queueNumber) from VisitCheckin c where c.queueDate = :queueDate")
    Optional<Integer> findMaxQueueNumber(@Param("queueDate") LocalDate queueDate);

    @Query("select c from VisitCheckin c " +
            "join fetch c.appointment a join fetch c.patient " +
            "left join fetch a.doctor left join fetch a.room left join fetch a.chair left join fetch a.workShift " +
            "where c.queueDate = :queueDate and c.status = :status " +
            "and (:doctorId is null or a.doctor.id = :doctorId) " +
            "order by c.queueNumber asc")
    List<VisitCheckin> findByQueueDateAndStatus(@Param("queueDate") LocalDate queueDate,
                                                  @Param("status") CheckinStatus status,
                                                  @Param("doctorId") Long doctorId);

    @Query("select count(c) from VisitCheckin c " +
            "where c.queueDate = :queueDate and c.appointment.doctor.id = :doctorId " +
            "and c.status <> com.smartdental.enums.CheckinStatus.CANCELLED")
    long countActiveByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("queueDate") LocalDate queueDate);

    long countByQueueDateAndStatus(LocalDate queueDate, CheckinStatus status);

    long countByQueueDateAndStatusIn(LocalDate queueDate, List<CheckinStatus> statuses);
}
