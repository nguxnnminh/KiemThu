package com.smartdental.repository;

import com.smartdental.entity.WorkShift;
import com.smartdental.enums.CommonStatus;
import com.smartdental.enums.WorkShiftDayType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {

    Optional<WorkShift> findByShiftCode(String shiftCode);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<WorkShift> findByStatus(CommonStatus status);

    @Query("select w from WorkShift w where w.status = :status " +
            "and (w.dayType = com.smartdental.enums.WorkShiftDayType.ALL or w.dayType = :dayType) " +
            "order by w.startTime asc")
    List<WorkShift> findActiveForDayType(@Param("status") CommonStatus status, @Param("dayType") WorkShiftDayType dayType);

    @Query("select w from WorkShift w where " +
            "(:keyword is null or lower(w.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(w.shiftCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:status is null or w.status = :status) " +
            "and (:dayType is null or w.dayType = :dayType)")
    Page<WorkShift> search(@Param("keyword") String keyword,
                            @Param("status") CommonStatus status,
                            @Param("dayType") WorkShiftDayType dayType,
                            Pageable pageable);
}
