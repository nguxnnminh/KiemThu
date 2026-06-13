package com.smartdental.repository;

import com.smartdental.entity.Patient;
import com.smartdental.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPhone(String phone);

    Optional<Patient> findByPatientCode(String patientCode);

    boolean existsByPhone(String phone);

    long countByStatus(CommonStatus status);

    @Query("select case when count(p) > 0 then true else false end from Patient p where p.phone = :phone and p.id <> :id")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("id") Long id);

    @Query("select p from Patient p where " +
            "(:keyword is null or lower(p.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(p.patientCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(p.phone) like lower(concat('%', :keyword, '%'))) " +
            "and (:status is null or p.status = :status)")
    Page<Patient> search(@Param("keyword") String keyword,
                          @Param("status") CommonStatus status,
                          Pageable pageable);
}
