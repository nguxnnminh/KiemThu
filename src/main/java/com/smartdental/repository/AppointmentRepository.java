package com.smartdental.repository;

import com.smartdental.entity.Appointment;
import com.smartdental.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    @Query("select a from Appointment a " +
            "left join fetch a.patient left join fetch a.doctor left join fetch a.service " +
            "left join fetch a.room left join fetch a.chair where " +
            "(:keyword is null or lower(a.patient.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.appointmentCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.patient.phone) like lower(concat('%', :keyword, '%'))) " +
            "and (:doctorId is null or a.doctor.id = :doctorId) " +
            "and (:patientId is null or a.patient.id = :patientId) " +
            "and (:status is null or a.status = :status) " +
            "and (:fromDate is null or a.appointmentDate >= :fromDate) " +
            "and (:toDate is null or a.appointmentDate <= :toDate)")
    Page<Appointment> search(@Param("keyword") String keyword,
                              @Param("doctorId") Long doctorId,
                              @Param("patientId") Long patientId,
                              @Param("status") AppointmentStatus status,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate,
                              Pageable pageable);

    @Query("select case when count(a) > 0 then true else false end from Appointment a " +
            "where a.doctor.id = :doctorId and a.appointmentDate = :date " +
            "and a.status not in ('CANCELLED', 'NO_SHOW') " +
            "and a.startTime < :endTime and a.endTime > :startTime " +
            "and (:excludeId is null or a.id <> :excludeId)")
    boolean existsDoctorConflict(@Param("doctorId") Long doctorId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("excludeId") Long excludeId);

    @Query("select case when count(a) > 0 then true else false end from Appointment a " +
            "where a.chair.id = :chairId and a.appointmentDate = :date " +
            "and a.status not in ('CANCELLED', 'NO_SHOW') " +
            "and a.startTime < :endTime and a.endTime > :startTime " +
            "and (:excludeId is null or a.id <> :excludeId)")
    boolean existsChairConflict(@Param("chairId") Long chairId,
                                 @Param("date") LocalDate date,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime,
                                 @Param("excludeId") Long excludeId);

    @Query("select case when count(a) > 0 then true else false end from Appointment a " +
            "where a.patient.id = :patientId and a.appointmentDate = :date and a.workShift.id = :workShiftId " +
            "and a.status not in ('CANCELLED', 'NO_SHOW') " +
            "and (:excludeId is null or a.id <> :excludeId)")
    boolean existsPatientConflictOnShift(@Param("patientId") Long patientId,
                                          @Param("date") LocalDate date,
                                          @Param("workShiftId") Long workShiftId,
                                          @Param("excludeId") Long excludeId);

    @Query("select count(a) from Appointment a " +
            "where a.patient.id = :patientId and a.appointmentDate >= :weekStart and a.appointmentDate <= :weekEnd " +
            "and a.status not in ('CANCELLED', 'NO_SHOW') " +
            "and (:excludeId is null or a.id <> :excludeId)")
    long countPatientAppointmentsInWeek(@Param("patientId") Long patientId,
                                         @Param("weekStart") LocalDate weekStart,
                                         @Param("weekEnd") LocalDate weekEnd,
                                         @Param("excludeId") Long excludeId);

    @Query("select count(a) from Appointment a " +
            "where a.doctorShiftRegistration.id = :registrationId " +
            "and a.status not in ('CANCELLED', 'NO_SHOW')")
    long countActiveByDoctorShiftRegistration(@Param("registrationId") Long registrationId);

    @Query("select case when count(a) > 0 then true else false end from Appointment a " +
            "where a.doctorShiftRegistration.id = :registrationId " +
            "and a.status not in ('CANCELLED', 'NO_SHOW', 'COMPLETED')")
    boolean existsUnfinishedByDoctorShiftRegistration(@Param("registrationId") Long registrationId);

    @Query("select a from Appointment a " +
            "left join fetch a.patient left join fetch a.doctor left join fetch a.service " +
            "left join fetch a.room left join fetch a.chair where " +
            "(:keyword is null or lower(a.patient.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.appointmentCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.patient.phone) like lower(concat('%', :keyword, '%'))) " +
            "and (:doctorId is null or a.doctor.id = :doctorId) " +
            "and (:patientId is null or a.patient.id = :patientId) " +
            "and (:status is null or a.status = :status) " +
            "and a.appointmentDate >= :fromDate and a.appointmentDate <= :toDate " +
            "order by a.appointmentDate asc, a.startTime asc")
    java.util.List<Appointment> findByDateRange(@Param("keyword") String keyword,
                                                  @Param("doctorId") Long doctorId,
                                                  @Param("patientId") Long patientId,
                                                  @Param("status") AppointmentStatus status,
                                                  @Param("fromDate") LocalDate fromDate,
                                                  @Param("toDate") LocalDate toDate);

    @Query("select a from Appointment a " +
            "left join fetch a.patient left join fetch a.doctor left join fetch a.service " +
            "left join fetch a.room left join fetch a.chair left join fetch a.workShift where " +
            "a.appointmentDate = :date and a.status = com.smartdental.enums.AppointmentStatus.CONFIRMED " +
            "and (:doctorId is null or a.doctor.id = :doctorId) " +
            "and (:workShiftId is null or a.workShift.id = :workShiftId) " +
            "and (:keyword is null or lower(a.patient.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.appointmentCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(a.patient.phone) like lower(concat('%', :keyword, '%'))) " +
            "order by a.startTime asc")
    java.util.List<Appointment> findConfirmedForCheckin(@Param("date") LocalDate date,
                                                          @Param("doctorId") Long doctorId,
                                                          @Param("workShiftId") Long workShiftId,
                                                          @Param("keyword") String keyword);
}
