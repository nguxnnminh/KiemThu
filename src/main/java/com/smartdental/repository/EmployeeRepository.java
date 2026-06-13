package com.smartdental.repository;

import com.smartdental.entity.Employee;
import com.smartdental.enums.DoctorDegree;
import com.smartdental.enums.EmployeePosition;
import com.smartdental.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    @Query("select case when count(e) > 0 then true else false end from Employee e where lower(e.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    @Query("select case when count(e) > 0 then true else false end from Employee e where lower(e.email) = lower(:email) and e.id <> :id")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("id") Long id);

    boolean existsByPhone(String phone);

    @Query("select case when count(e) > 0 then true else false end from Employee e where e.phone = :phone and e.id <> :id")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("id") Long id);

    List<Employee> findByPositionAndStatus(EmployeePosition position, EmployeeStatus status);

    List<Employee> findByPosition(EmployeePosition position);

    long countByStatus(EmployeeStatus status);

    long countByPosition(EmployeePosition position);

    @Query("select e from Employee e where " +
            "(:keyword is null or lower(e.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(e.employeeCode) like lower(concat('%', :keyword, '%')) " +
            "or lower(e.phone) like lower(concat('%', :keyword, '%')) " +
            "or lower(e.email) like lower(concat('%', :keyword, '%'))) " +
            "and (:position is null or e.position = :position) " +
            "and (:status is null or e.status = :status) " +
            "and (:degree is null or e.degree = :degree)")
    Page<Employee> search(@Param("keyword") String keyword,
                           @Param("position") EmployeePosition position,
                           @Param("status") EmployeeStatus status,
                           @Param("degree") DoctorDegree degree,
                           Pageable pageable);
}
