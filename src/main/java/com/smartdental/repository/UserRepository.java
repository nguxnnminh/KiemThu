package com.smartdental.repository;

import com.smartdental.entity.User;
import com.smartdental.enums.Role;
import com.smartdental.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where lower(u.username) = lower(:username)")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("select u from User u left join fetch u.employee left join fetch u.patient where lower(u.username) = lower(:username)")
    Optional<User> findByUsernameIgnoreCaseFetchEmployeeAndPatient(@Param("username") String username);

    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.username) = lower(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.email) = lower(:email) and u.id <> :id")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query("select count(u) from User u where u.role = com.smartdental.enums.Role.ADMIN " +
            "and u.status = com.smartdental.enums.UserStatus.ACTIVE")
    long countActiveAdmins();

    @Query("select u from User u where u.role = :role")
    java.util.List<User> findByRole(@Param("role") Role role);

    boolean existsByEmployeeId(Long employeeId);

    Optional<User> findByEmployeeId(Long employeeId);

    boolean existsByPatientId(Long patientId);

    @Query("select case when count(u) > 0 then true else false end from User u where u.employee.id = :employeeId and u.id <> :id")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") Long employeeId, @Param("id") Long id);

    @Query(value = "select u from User u " +
            "left join fetch u.employee e " +
            "left join fetch u.patient p " +
            "where " +
            "(:keyword is null or lower(u.username) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.email) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(e.fullName, '')) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(p.fullName, '')) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(e.employeeCode, '')) like lower(concat('%', :keyword, '%')) " +
            "or lower(coalesce(p.patientCode, '')) like lower(concat('%', :keyword, '%')) ) " +
            "and (:role is null or u.role = :role) " +
            "and (:status is null or u.status = :status)",
            countQuery = "select count(u) from User u " +
                    "left join u.employee e " +
                    "left join u.patient p " +
                    "where " +
                    "(:keyword is null or lower(u.username) like lower(concat('%', :keyword, '%')) " +
                    "or lower(u.email) like lower(concat('%', :keyword, '%')) " +
                    "or lower(coalesce(e.fullName, '')) like lower(concat('%', :keyword, '%')) " +
                    "or lower(coalesce(p.fullName, '')) like lower(concat('%', :keyword, '%')) " +
                    "or lower(coalesce(e.employeeCode, '')) like lower(concat('%', :keyword, '%')) " +
                    "or lower(coalesce(p.patientCode, '')) like lower(concat('%', :keyword, '%')) ) " +
                    "and (:role is null or u.role = :role) " +
                    "and (:status is null or u.status = :status)")
    Page<User> search(@Param("keyword") String keyword,
                       @Param("role") Role role,
                       @Param("status") UserStatus status,
                       Pageable pageable);
}
