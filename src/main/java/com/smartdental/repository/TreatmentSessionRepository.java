package com.smartdental.repository;

import com.smartdental.entity.TreatmentSession;
import com.smartdental.enums.TreatmentSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TreatmentSessionRepository extends JpaRepository<TreatmentSession, Long> {

    Optional<TreatmentSession> findBySessionCode(String sessionCode);

    Optional<TreatmentSession> findByAppointmentId(Long appointmentId);

    @Query("select s from TreatmentSession s " +
            "join fetch s.patient join fetch s.doctor join fetch s.medicalRecord join fetch s.appointment a join fetch s.checkin " +
            "left join fetch a.room left join fetch a.chair left join fetch a.workShift " +
            "where s.id = :id")
    Optional<TreatmentSession> findDetailById(@Param("id") Long id);

    @Query("select case when count(s) > 0 then true else false end from TreatmentSession s " +
            "where s.patient.id = :patientId and s.status = com.smartdental.enums.TreatmentSessionStatus.OPEN")
    boolean existsOpenByPatientId(@Param("patientId") Long patientId);

    boolean existsByDoctorIdAndStatus(Long doctorId, TreatmentSessionStatus status);

    List<TreatmentSession> findByPatientIdOrderByExaminationDateDesc(Long patientId);

    @Query("select s from TreatmentSession s join fetch s.patient join fetch s.doctor " +
            "where s.doctor.id = :doctorId order by s.examinationDate desc")
    List<TreatmentSession> findByDoctorIdOrderByExaminationDateDesc(@Param("doctorId") Long doctorId);

    @Query("select s from TreatmentSession s " +
            "join fetch s.patient p join fetch s.doctor d join fetch s.medicalRecord " +
            "where (:doctorId is null or d.id = :doctorId) " +
            "and (:keyword is null or lower(p.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(p.patientCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(p.phone, '')) like lower(concat('%', :keyword, '%')) " +
            "or lower(s.sessionCode) like lower(concat('%', :keyword, '%'))) " +
            "order by s.examinationDate desc, s.id desc")
    List<TreatmentSession> findMedicalRecordRows(@Param("keyword") String keyword, @Param("doctorId") Long doctorId);

    @Query("select s from TreatmentSession s " +
            "where s.status = com.smartdental.enums.TreatmentSessionStatus.COMPLETED " +
            "and (:fromDate is null or s.examinationDate >= :fromDate) " +
            "and (:toDate is null or s.examinationDate <= :toDate) " +
            "and (:doctorId is null or s.doctor.id = :doctorId)")
    List<TreatmentSession> findCompletedForReport(@Param("fromDate") LocalDate fromDate,
                                                    @Param("toDate") LocalDate toDate,
                                                    @Param("doctorId") Long doctorId);

    @Query("select s from TreatmentSession s " +
            "where s.appointment.doctorShiftRegistration.id = :registrationId " +
            "and s.status = com.smartdental.enums.TreatmentSessionStatus.COMPLETED")
    List<TreatmentSession> findCompletedByDoctorShiftRegistrationId(@Param("registrationId") Long registrationId);

    @Query("select s from TreatmentSession s " +
            "join fetch s.patient join fetch s.doctor join fetch s.appointment a " +
            "left join fetch a.workShift left join fetch a.doctorShiftRegistration r left join fetch r.workShift " +
            "where s.doctor.id = :doctorId " +
            "and s.status = com.smartdental.enums.TreatmentSessionStatus.COMPLETED " +
            "and s.examinationDate >= :fromDate and s.examinationDate <= :toDate " +
            "order by s.examinationDate asc, s.id asc")
    List<TreatmentSession> findCompletedForPayroll(@Param("doctorId") Long doctorId,
                                                    @Param("fromDate") LocalDate fromDate,
                                                    @Param("toDate") LocalDate toDate);
}
