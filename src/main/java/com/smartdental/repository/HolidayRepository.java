package com.smartdental.repository;

import com.smartdental.entity.Holiday;
import com.smartdental.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    Optional<Holiday> findByHolidayCode(String holidayCode);

    @Query("select case when count(h) > 0 then true else false end from Holiday h " +
            "where lower(h.name) = lower(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("select case when count(h) > 0 then true else false end from Holiday h " +
            "where lower(h.name) = lower(:name) and h.id <> :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("select h from Holiday h where " +
            "(:keyword is null or lower(h.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(h.holidayCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:holidayType is null or h.holidayType = :holidayType) " +
            "and (:status is null or h.status = :status)")
    Page<Holiday> search(@Param("keyword") String keyword,
                          @Param("holidayType") com.smartdental.enums.HolidayType holidayType,
                          @Param("status") CommonStatus status,
                          Pageable pageable);

    @Query("select case when count(h) > 0 then true else false end from Holiday h " +
            "where h.status = 'ACTIVE' and h.startDate <= :date and h.endDate >= :date")
    boolean existsActiveHolidayOnDate(@Param("date") LocalDate date);
}
