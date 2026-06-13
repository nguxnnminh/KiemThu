package com.smartdental.repository;

import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.enums.DoctorShiftStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorShiftRegistrationRepository extends JpaRepository<DoctorShiftRegistration, Long> {

    Optional<DoctorShiftRegistration> findByRegistrationCode(String registrationCode);

    boolean existsByDoctorIdAndWorkShiftIdAndWorkDate(Long doctorId, Long workShiftId, LocalDate workDate);

    @Query("select d from DoctorShiftRegistration d " +
            "join fetch d.doctor join fetch d.workShift left join fetch d.room left join fetch d.chair where " +
            "(:doctorId is null or d.doctor.id = :doctorId) " +
            "and (:fromDate is null or d.workDate >= :fromDate) " +
            "and (:toDate is null or d.workDate <= :toDate) " +
            "and (:status is null or d.status = :status)")
    Page<DoctorShiftRegistration> search(@Param("doctorId") Long doctorId,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate,
                                          @Param("status") DoctorShiftStatus status,
                                          Pageable pageable);

    @Query("select d from DoctorShiftRegistration d " +
            "join fetch d.doctor join fetch d.workShift left join fetch d.room left join fetch d.chair " +
            "where d.workDate = :date and d.status = 'APPROVED'")
    List<DoctorShiftRegistration> findApprovedByWorkDate(@Param("date") LocalDate date);

    @Query("select d from DoctorShiftRegistration d " +
            "join fetch d.doctor join fetch d.workShift left join fetch d.room left join fetch d.chair where " +
            "(:doctorId is null or d.doctor.id = :doctorId) " +
            "and d.workDate >= :fromDate and d.workDate <= :toDate " +
            "and (:status is null or d.status = :status) " +
            "order by d.workDate asc")
    List<DoctorShiftRegistration> findByDateRange(@Param("doctorId") Long doctorId,
                                                     @Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate,
                                                     @Param("status") DoctorShiftStatus status);

    @Query("select case when count(d) > 0 then true else false end from DoctorShiftRegistration d " +
            "where d.doctor.id = :doctorId and d.workDate = :date and d.status = 'APPROVED'")
    boolean existsApprovedForDoctorOnDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("select d from DoctorShiftRegistration d " +
            "join fetch d.doctor join fetch d.workShift join fetch d.room join fetch d.chair " +
            "where d.doctor.id = :doctorId and d.workDate = :date and d.workShift.id = :workShiftId and d.status = 'APPROVED'")
    Optional<DoctorShiftRegistration> findConfirmedByDoctorDateAndShift(@Param("doctorId") Long doctorId,
                                                                          @Param("date") LocalDate date,
                                                                          @Param("workShiftId") Long workShiftId);

    @Query("select case when count(d) > 0 then true else false end from DoctorShiftRegistration d " +
            "where d.doctor.id = :doctorId and d.workDate = :date and d.workShift.id = :workShiftId " +
            "and d.status in ('REGISTERED', 'APPROVED') " +
            "and (:excludeId is null or d.id <> :excludeId)")
    boolean existsDoctorConflict(@Param("doctorId") Long doctorId,
                                  @Param("date") LocalDate date,
                                  @Param("workShiftId") Long workShiftId,
                                  @Param("excludeId") Long excludeId);

    @Query("select case when count(d) > 0 then true else false end from DoctorShiftRegistration d " +
            "where d.chair.id = :chairId and d.workDate = :date and d.workShift.id = :workShiftId " +
            "and d.status in ('REGISTERED', 'APPROVED') " +
            "and (:excludeId is null or d.id <> :excludeId)")
    boolean existsChairConflict(@Param("chairId") Long chairId,
                                 @Param("date") LocalDate date,
                                 @Param("workShiftId") Long workShiftId,
                                 @Param("excludeId") Long excludeId);

    @Query("select d from DoctorShiftRegistration d " +
            "join fetch d.workShift left join fetch d.room left join fetch d.chair " +
            "where d.doctor.id = :doctorId and d.status = com.smartdental.enums.DoctorShiftStatus.APPROVED " +
            "and d.workDate >= :fromDate and d.workDate <= :toDate order by d.workDate")
    List<DoctorShiftRegistration> findApprovedByDoctorAndPeriod(@Param("doctorId") Long doctorId,
                                                                  @Param("fromDate") LocalDate fromDate,
                                                                  @Param("toDate") LocalDate toDate);

    @Query("select distinct d.doctor.id from DoctorShiftRegistration d " +
            "where d.status = com.smartdental.enums.DoctorShiftStatus.APPROVED " +
            "and d.workDate >= :fromDate and d.workDate <= :toDate")
    List<Long> findDoctorIdsWithApprovedShiftsInPeriod(@Param("fromDate") LocalDate fromDate,
                                                         @Param("toDate") LocalDate toDate);
}
